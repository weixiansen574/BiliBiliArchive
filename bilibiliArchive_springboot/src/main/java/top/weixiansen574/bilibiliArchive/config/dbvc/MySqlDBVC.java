package top.weixiansen574.bilibiliArchive.config.dbvc;

import java.sql.*;

public abstract class MySqlDBVC extends DBVersionController {

    public MySqlDBVC(Connection connection, int version) {
        super(connection, version);
    }

    @Override
    public int getDBVersion() throws SQLException {
        initMetaDataTable();

        String sql = "SELECT value FROM meta_data WHERE `key` = 'db_version'";
        try (PreparedStatement preparedStatement = dbExe.connection().prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Integer.parseInt(resultSet.getString("value")); // 从 TEXT 类型转换为整数
            }
        }

        return 0;
    }

    @Override
    public void setDBVersion(int newVersion) throws SQLException {
        String updateSql = "UPDATE meta_data SET value = ? WHERE `key` = 'db_version'";
        try (PreparedStatement preparedStatement = dbExe.connection().prepareStatement(updateSql)) {
            preparedStatement.setString(1, String.valueOf(newVersion));
            preparedStatement.executeUpdate();
        }
    }

    private void initMetaDataTable() throws SQLException {
        if (!isTableExists(connection, "meta_data")) {
            String createTableSql = """
                        CREATE TABLE IF NOT EXISTS meta_data (
                            `key` varchar(255) NOT NULL,
                            `value` text,
                            PRIMARY KEY (`key`)
                        )
                    """;
            Statement statement = connection.createStatement();
            statement.execute(createTableSql);
            statement.executeUpdate("INSERT INTO meta_data (`key`, value) VALUES ('db_version', 0)");
            statement.close();
        }
    }

    public boolean isTableExists(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW TABLES LIKE 'meta_data'");
        return resultSet.next();
    }
}
