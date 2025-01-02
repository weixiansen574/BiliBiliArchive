package top.weixiansen574.bilibiliArchive.config.dbvc.master;

import top.weixiansen574.bilibiliArchive.config.dbvc.SQLiteDBVC;

import java.sql.Connection;
import java.sql.SQLException;

public class MasterSQLiteDBVC extends SQLiteDBVC {
    public static final int VERSION = 1;

    public MasterSQLiteDBVC(Connection connection) {
        super(connection, VERSION);
    }

    @Override
    public void onCreate() throws SQLException {
        executeSQLFromResource("sql/sqlite/master_init.sql",connection);
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) throws SQLException {

    }
}
