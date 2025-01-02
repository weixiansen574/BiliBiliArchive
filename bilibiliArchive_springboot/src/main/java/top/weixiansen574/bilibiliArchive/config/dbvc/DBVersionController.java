package top.weixiansen574.bilibiliArchive.config.dbvc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public abstract class DBVersionController {
    public final DBExe dbExe;
    public final Connection connection;
    public final int version;

    public DBVersionController(Connection connection, int version) {
        this.connection = connection;
        this.dbExe = new DBExe(connection);
        this.version = version;
    }

    public final void doInit() throws SQLException {
        int dbVersion = getDBVersion();
        if (dbVersion == 0){
            onCreate();
            setDBVersion(version);
        } else if (version > dbVersion){
            onUpgrade(dbVersion,version);
            setDBVersion(version);
        }
        dbExe.connection().close();
    }

    public abstract void onCreate() throws SQLException;

    public abstract void onUpgrade(int oldVersion, int newVersion) throws SQLException;

    public final int getVersion(){
        return version;
    }

    public abstract int getDBVersion() throws SQLException;

    public abstract void setDBVersion(int newVersion) throws SQLException;

    public void executeSQLFromResource(String resourcePath, Connection connection) throws SQLException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resourcePath))))) {

            String line;
            StringBuilder sqlBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // 跳过注释行
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sqlBuilder.append(line).append("\n");
                }
            }

            // 按分号拆分SQL语句
            String[] sqlStatements = sqlBuilder.toString().split(";");
            connection.setAutoCommit(false);  // 开启事务管理

            try (Statement statement = connection.createStatement()) {
                for (String sql : sqlStatements) {
                    String trimmedStatement = sql.trim();
                    if (!trimmedStatement.isEmpty()) {
                        statement.execute(trimmedStatement);
                    }
                }
                connection.commit();  // 提交事务
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                connection.rollback();  // 发生错误时回滚事务
                connection.setAutoCommit(true);
                throw e;
            }
        } catch (Exception e) {
            throw new SQLException("Failed to execute SQL from resource", e);
        }
    }

}
