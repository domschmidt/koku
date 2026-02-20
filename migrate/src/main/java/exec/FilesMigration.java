package exec;

import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

public class FilesMigration extends BaseMigration {

    private final Connection customersTarget;
    private final String uploadsDir;

    public FilesMigration(Connection source, Connection target, Connection customersTarget, String uploadsDir) {
        super(source, target);

        this.customersTarget = customersTarget;
        this.uploadsDir = uploadsDir;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating Files...");

        Map<String, Long> customerExternalRefMapping = new HashMap<>();
        read(
                "SELECT id, external_ref FROM koku.customer",
                rs -> {
                    try {
                        customerExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                this.customersTarget);

        read("""
                     SELECT uuid, creation_date, deleted, file_name, customer_id, size, media_type
                     FROM koku.file;
                """, rs -> {
            try {
                String originCustomerId = rs.getString("customer_id");
                Long mappedCustomerId = customerExternalRefMapping.get(originCustomerId);

                String fileUUID = rs.getString("uuid");
                String fileName = rs.getString("file_name");
                Path filePath =
                        Paths.get(uploadsDir, String.format("%s.%s", fileUUID, FilenameUtils.getExtension(fileName)));
                byte[] content = Files.readAllBytes(filePath);

                String mime_type = rs.getString("media_type");
                if (mime_type == null) {
                    mime_type = Files.probeContentType(filePath);
                }
                if (mime_type == null) {
                    mime_type = URLConnection.guessContentTypeFromName(fileName);
                }
                if (mime_type == null) {
                    mime_type = "application/octet-stream";
                }

                exec(
                        """
                                INSERT INTO koku.file (external_ref, recorded, updated, deleted, filename, content, mime_type, size, customer_id)
                                VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?, ?, ?)
                                ON CONFLICT (external_ref)
                                DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                              updated = EXCLUDED.updated,
                                              deleted = EXCLUDED.deleted,
                                              filename = EXCLUDED.filename,
                                              content = EXCLUDED.content,
                                              mime_type = EXCLUDED.mime_type,
                                              size = EXCLUDED.size,
                                              customer_id = EXCLUDED.customer_id,
                                              version = "file".version + 1
                                WHERE EXCLUDED.updated > "file".updated;
                                """,
                        fileUUID,
                        rs.getTimestamp("creation_date"),
                        rs.getTimestamp("creation_date"),
                        rs.getBoolean("deleted"),
                        fileName,
                        content,
                        mime_type,
                        rs.getLong("size"),
                        mappedCustomerId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ Files done.");
    }
}
