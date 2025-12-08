import org.apache.commons.io.IOUtils;

import java.sql.Connection;
import java.util.Map;

public class UserMigration extends BaseMigration {

    private final Map<String, String> userMapping;

    public UserMigration(Connection source, Connection target, Map<String, String> userMapping) {
        super(source, target);
        this.userMapping = userMapping;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating User...");

        read("""
                     SELECT usr.id, usr.recorded, usr.updated, usr.deleted, usr.username, details.avatar_base64, details.firstname, details.lastname 
                     FROM koku.user usr
                     LEFT OUTER JOIN koku.user_details details ON (details.id = usr.user_details_id);
                """, rs -> {
            try {
                String originUserId = rs.getString("id");
                String mappedUserId = this.userMapping.get(originUserId);
                if (mappedUserId != null) {
                    String avatarResult = null;
                    String avatarBase64Raw = IOUtils.toString(rs.getClob("avatar_base64").getCharacterStream());
                    if (avatarBase64Raw != null && !avatarBase64Raw.trim().isEmpty()) {
                        avatarResult = String.format("data:image/png;base64,%s", avatarBase64Raw);
                    }
                    exec("""
                                    INSERT INTO koku.user (id, external_ref, recorded, updated, deleted, firstname, lastname, fullname, avatar_base64)
                                    VALUES (?, ?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?, ?)
                                    ON CONFLICT (external_ref)
                                    DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                                  updated = EXCLUDED.updated,
                                                  id = EXCLUDED.id,
                                                  deleted = EXCLUDED.deleted,
                                                  firstname = EXCLUDED.firstname,
                                                  lastname = EXCLUDED.lastname,
                                                  fullname = EXCLUDED.fullname,
                                                  avatar_base64 = EXCLUDED.avatar_base64,
                                                  version = "user".version + 1
                                    WHERE EXCLUDED.updated > "user".updated;
                                    """,
                            mappedUserId,
                            rs.getString("id"),
                            rs.getTimestamp("recorded"),
                            rs.getTimestamp("updated"),
                            rs.getTimestamp("updated"),
                            rs.getBoolean("deleted"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            String.format("%s %s", rs.getString("firstname"), rs.getString("lastname")).trim(),
                            avatarResult
                    );
                } else {
                    System.err.printf("%s is not available in user mapping", originUserId);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ User done.");
    }
}
