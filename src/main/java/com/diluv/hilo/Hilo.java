package com.diluv.hilo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;

import com.diluv.hilo.database.FileDatabase;
import com.diluv.hilo.database.records.FileQueueRecord;
import com.diluv.hilo.utils.Constants;
import com.zaxxer.hikari.HikariDataSource;

public class Hilo {

    private static HikariDataSource ds;
    private static Connection connection;

    public static void main (String[] args) {

        ds = new HikariDataSource();
        ds.setJdbcUrl(Constants.DB_HOSTNAME);
        ds.setUsername(Constants.DB_USERNAME);
        ds.setPassword(Constants.DB_PASSWORD);
        ds.addDataSourceProperty("rewriteBatchedStatements", "true");

        FileDatabase test = new FileDatabase();
        try {
            List<FileQueueRecord> projectFiles = test.getLatestFileQueueRecord(10);
            System.out.println(projectFiles);
        }
        catch (SQLTransactionRollbackException e) {
            // TODO Couldn't get a lock (Aka something is causing a lock aka another thread), try again
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connection () throws SQLException {

        if (connection == null || connection.isClosed())
            connection = ds.getConnection();
        return connection;
    }
}