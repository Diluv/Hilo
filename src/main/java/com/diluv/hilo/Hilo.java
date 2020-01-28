package com.diluv.hilo;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;

import com.diluv.confluencia.Confluencia;
import com.diluv.confluencia.database.FileDatabase;
import com.diluv.confluencia.database.dao.FileDAO;
import com.diluv.confluencia.database.record.ProjectFileQueueRecord;
import com.diluv.hilo.utils.Constants;

public class Hilo {

    public static void main (String[] args) {

        Confluencia.init(Constants.DB_HOSTNAME, Constants.DB_USERNAME, Constants.DB_PASSWORD, false);

        final FileDAO fileDAO = new FileDatabase();
        try {
            final List<ProjectFileQueueRecord> projectFiles = fileDAO.getLatestFileQueueRecord(10);
            System.out.println(projectFiles);
        }
        catch (final SQLTransactionRollbackException e) {
            // TODO Couldn't get a lock (Aka something is causing a lock aka
            // another thread), try again
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}