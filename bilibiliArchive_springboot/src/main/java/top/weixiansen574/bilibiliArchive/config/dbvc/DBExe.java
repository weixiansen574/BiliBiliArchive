package top.weixiansen574.bilibiliArchive.config.dbvc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public record DBExe(Connection connection) {

    public void execSQL(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
    }

}
