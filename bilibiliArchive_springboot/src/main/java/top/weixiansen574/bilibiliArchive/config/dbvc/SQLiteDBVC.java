package top.weixiansen574.bilibiliArchive.config.dbvc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLiteDBVC extends DBVersionController {
    public SQLiteDBVC(Connection connection, int version) {
        super(connection, version);
    }

    @Override
    public int getDBVersion() throws SQLException {
        Statement statement = dbExe.connection().createStatement();
        ResultSet resultSet = statement.executeQuery("PRAGMA user_version;");
        int userVersion = -1;
        if (resultSet.next()) {
            userVersion = resultSet.getInt(1);
        }
        resultSet.close();
        statement.close();
        return userVersion;
    }

    @Override
    public void setDBVersion(int newVersion) throws SQLException{
        Statement statement = dbExe.connection().createStatement();
        statement.execute("PRAGMA user_version = " + newVersion);
        statement.close();
    }
}
