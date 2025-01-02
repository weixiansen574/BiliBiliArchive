package top.weixiansen574.bilibiliArchive.config.dbvc.comment;

import top.weixiansen574.bilibiliArchive.config.dbvc.MySqlDBVC;

import java.sql.Connection;
import java.sql.SQLException;

public class CommentMySqlDBVC extends MySqlDBVC {

    public CommentMySqlDBVC(Connection connection) {
        super(connection, 1);
    }

    @Override
    public void onCreate() throws SQLException {
        executeSQLFromResource("sql/mysql/comments_init.sql",connection);
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) throws SQLException {

    }
}
