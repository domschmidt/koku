package exec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class ModuleContractTest {

    @Test
    void allMigrationsTransferARepresentativeRow(@TempDir final Path uploads) throws Exception {
        Files.writeString(uploads.resolve("1."), "content");
        final Connection connection = successfulConnection();
        final Map<String, String> userMapping = Map.of("1", "mapped-user");
        final List<BaseMigration> migrations = List.of(
                new ProductManufacturerMigration(connection, connection),
                new ProductMigration(connection, connection),
                new ProductPriceMigration(connection, connection),
                new PromotionMigration(connection, connection),
                new ActivityStepMigration(connection, connection),
                new ActivityMigration(connection, connection),
                new ActivityPriceMigration(connection, connection),
                new UserMigration(connection, connection, userMapping),
                new UserAppointmentMigration(connection, connection),
                new CustomerMigration(connection, connection),
                new CustomerAppointmentMigration(connection, connection, userMapping),
                new CustomerAppointmentActivityMigration(connection, connection, connection),
                new CustomerAppointmentActivitySequenceMigration(connection, connection, connection, connection),
                new CustomerAppointmentPromotionMigration(connection, connection, connection),
                new CustomerAppointmentSoldProductMigration(connection, connection, connection),
                new FilesMigration(connection, connection, connection, uploads.toString()));

        migrations.forEach(migration -> assertDoesNotThrow(migration::migrate));
    }

    @Test
    void baseMigrationWrapsSqlFailures() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("database unavailable"));
        final TestMigration migration = new TestMigration(connection);

        assertThrows(IllegalStateException.class, migration::readFailure);
        assertThrows(IllegalStateException.class, migration::execFailure);
    }

    @Test
    void allMigrationsWrapRowFailures(@TempDir final Path uploads) throws Exception {
        final Connection connection = failingRowConnection();
        final Map<String, String> userMapping = Map.of("1", "mapped-user");
        final List<BaseMigration> migrations = List.of(
                new ProductManufacturerMigration(connection, connection),
                new ProductMigration(connection, connection),
                new ProductPriceMigration(connection, connection),
                new PromotionMigration(connection, connection),
                new ActivityStepMigration(connection, connection),
                new ActivityMigration(connection, connection),
                new ActivityPriceMigration(connection, connection),
                new UserMigration(connection, connection, userMapping),
                new UserAppointmentMigration(connection, connection),
                new CustomerMigration(connection, connection),
                new CustomerAppointmentMigration(connection, connection, userMapping),
                new CustomerAppointmentActivityMigration(connection, connection, connection),
                new CustomerAppointmentActivitySequenceMigration(connection, connection, connection, connection),
                new CustomerAppointmentPromotionMigration(connection, connection, connection),
                new CustomerAppointmentSoldProductMigration(connection, connection, connection),
                new FilesMigration(connection, connection, connection, uploads.toString()));

        migrations.forEach(migration -> assertThrows(MigrationException.class, migration::migrate));
        final SQLException cause = new SQLException("cause");
        assertSame(cause, new MigrationException("message", cause).getCause());
    }

    @Test
    void runnerExecutesAllMigrations(@TempDir final Path uploads) throws Exception {
        Files.writeString(uploads.resolve("1."), "content");
        final Connection connection = successfulConnection();
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(connection);

            assertDoesNotThrow(() -> MigrateRunner.main(new String[] {
                "source",
                "products",
                "promotions",
                "activities",
                "users",
                "customers",
                "files",
                uploads.toString(),
                "1:mapped-user"
            }));
        }
    }

    @Test
    void runnerReadsLegacyEnvironmentConfiguration() {
        new MigrateRunner();
        assertArrayEquals(
                new String[] {
                    "db_source",
                    "db_target_products",
                    "db_target_promotions",
                    "db_target_activities",
                    "db_target_users",
                    "db_target_customers",
                    "db_target_files",
                    "dir_source_uploads",
                    "usermapping"
                },
                MigrateRunner.configuration(new String[0], key -> key));
    }

    @Test
    void mappedMigrationsWrapFailuresInTheirMainRows(@TempDir final Path uploads) throws Exception {
        final BaseMigration activity = new CustomerAppointmentActivityMigration(
                connectionWithRows(failingRow()), successfulConnection(), connectionWithRows(emptyRow()));
        final BaseMigration promotion = new CustomerAppointmentPromotionMigration(
                connectionWithRows(failingRow()), successfulConnection(), connectionWithRows(emptyRow()));
        final BaseMigration soldProduct = new CustomerAppointmentSoldProductMigration(
                connectionWithRows(failingRow()), successfulConnection(), connectionWithRows(emptyRow()));
        final BaseMigration activitySequence = new CustomerAppointmentActivitySequenceMigration(
                connectionWithRows(failingRow()),
                successfulConnection(),
                connectionWithRows(emptyRow()),
                connectionWithRows(emptyRow()));
        final BaseMigration activitySequenceProduct = new CustomerAppointmentActivitySequenceMigration(
                connectionWithRows(emptyRow()),
                successfulConnection(),
                connectionWithRows(representativeRow()),
                connectionWithRows(failingRow()));
        final BaseMigration files = new FilesMigration(
                connectionWithRows(failingRow()),
                successfulConnection(),
                connectionWithRows(emptyRow()),
                uploads.toString());

        assertThrows(MigrationException.class, activity::migrate);
        assertThrows(MigrationException.class, promotion::migrate);
        assertThrows(MigrationException.class, soldProduct::migrate);
        assertThrows(MigrationException.class, activitySequence::migrate);
        assertThrows(MigrationException.class, activitySequenceProduct::migrate);
        assertThrows(MigrationException.class, files::migrate);
    }

    @Test
    void mappedMigrationsCoverAlternativeRows(@TempDir final Path uploads) throws Exception {
        assertDoesNotThrow(() -> new CustomerAppointmentActivityMigration(
                        connectionWithRows(representativeRow()), successfulConnection(), connectionWithRows(emptyRow()))
                .migrate());
        assertDoesNotThrow(() -> new UserMigration(successfulConnection(), successfulConnection(), Map.of()).migrate());
        assertDoesNotThrow(() ->
                new CustomerAppointmentMigration(successfulConnection(), successfulConnection(), Map.of()).migrate());

        Files.writeString(uploads.resolve("1."), "content");
        final ResultSet fileRow = representativeRow();
        when(fileRow.getString("media_type")).thenReturn(null);
        assertDoesNotThrow(() -> new FilesMigration(
                        connectionWithRows(fileRow),
                        successfulConnection(),
                        connectionWithRows(emptyRow()),
                        uploads.toString())
                .migrate());

        final ResultSet appointmentRow = representativeRow();
        when(appointmentRow.getTimestamp("ending")).thenReturn(null);
        assertDoesNotThrow(() ->
                new UserAppointmentMigration(connectionWithRows(appointmentRow), successfulConnection()).migrate());

        new TestMigration(successfulConnection()).warning();
    }

    private static Connection successfulConnection() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            final PreparedStatement statement = mock(PreparedStatement.class);
            when(statement.executeQuery()).thenAnswer(ignored -> representativeRow());
            when(statement.executeUpdate()).thenReturn(1);
            return statement;
        });
        return connection;
    }

    private static Connection failingRowConnection() throws Exception {
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            final PreparedStatement statement = mock(PreparedStatement.class);
            final ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(anyString())).thenThrow(new SQLException("invalid row"));
            when(statement.executeQuery()).thenReturn(resultSet);
            return statement;
        });
        return connection;
    }

    private static Connection connectionWithRows(final ResultSet... rows) throws Exception {
        final ArrayDeque<ResultSet> remainingRows = new ArrayDeque<>(List.of(rows));
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            final PreparedStatement statement = mock(PreparedStatement.class);
            when(statement.executeQuery()).thenReturn(remainingRows.removeFirst());
            when(statement.executeUpdate()).thenReturn(1);
            return statement;
        });
        return connection;
    }

    private static ResultSet emptyRow() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        return resultSet;
    }

    private static ResultSet failingRow() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(anyString())).thenThrow(new SQLException("invalid row"));
        return resultSet;
    }

    private static ResultSet representativeRow() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(anyString())).thenReturn("1");
        when(resultSet.getLong(anyString())).thenReturn(1L);
        when(resultSet.getInt(anyString())).thenReturn(1);
        when(resultSet.getBoolean(anyString())).thenReturn(false);
        when(resultSet.getBigDecimal(anyString())).thenReturn(BigDecimal.ONE);
        when(resultSet.getTimestamp(anyString())).thenReturn(Timestamp.from(Instant.parse("2026-07-14T10:00:00Z")));
        when(resultSet.getDate(anyString())).thenReturn(Date.valueOf("2026-07-14"));
        final Clob clob = mock(Clob.class);
        when(clob.getCharacterStream()).thenReturn(new StringReader("avatar"));
        when(resultSet.getClob(anyString())).thenReturn(clob);
        return resultSet;
    }

    private static final class TestMigration extends BaseMigration {
        private TestMigration(final Connection connection) {
            super(connection, connection);
        }

        @Override
        public void migrate() {
            // No migration work is required for the BaseMigration contract test double.
        }

        private void readFailure() {
            read("SELECT 1", ignored -> {});
        }

        private void execFailure() {
            exec("UPDATE test SET value = ?", "value");
        }

        private void warning() {
            logWarning("warning");
        }
    }
}
