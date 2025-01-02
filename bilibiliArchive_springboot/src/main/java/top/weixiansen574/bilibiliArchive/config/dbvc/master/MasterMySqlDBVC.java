package top.weixiansen574.bilibiliArchive.config.dbvc.master;

import top.weixiansen574.bilibiliArchive.config.dbvc.MySqlDBVC;

import java.sql.Connection;
import java.sql.SQLException;

public class MasterMySqlDBVC extends MySqlDBVC {
    public MasterMySqlDBVC(Connection connection) {
        super(connection, 1);
    }

    @Override
    public void onCreate() throws SQLException {
        executeSQLFromResource("sql/mysql/master_init.sql",connection);
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) throws SQLException {

    }
}
