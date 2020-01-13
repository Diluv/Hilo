package com.diluv.hilo.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.diluv.hilo.Hilo;
import com.diluv.hilo.database.dao.FileDAO;
import com.diluv.hilo.database.records.FileQueueRecord;
import com.diluv.hilo.utils.SQLHandler;

public class FileDatabase implements FileDAO {

    private static final String FIND_ALL_WHERE_PENDING = SQLHandler.readFile("file_queue/findOneWherePending");
    private static final String UPDATE_STATUS_BY_ID = SQLHandler.readFile("file_queue/updateStatusById");

    @Override
    public List<FileQueueRecord> findAllWherePending (int amount) {

        List<FileQueueRecord> fileQueueRecord = new ArrayList<>();
        try (PreparedStatement stmt = Hilo.connection().prepareStatement(FIND_ALL_WHERE_PENDING)) {
            stmt.setInt(1, amount);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fileQueueRecord.add(new FileQueueRecord(rs));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return fileQueueRecord;
    }

    @Override
    public boolean updateFileQueueStatusById (long id) throws SQLException {

        try (PreparedStatement stmt = Hilo.connection().prepareStatement(UPDATE_STATUS_BY_ID)) {
            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 1) {
                return true;
            }
        }
        catch (SQLException e) {
            Hilo.connection().rollback();
            throw e;
        }
        return false;
    }

    @Override
    public List<FileQueueRecord> getLatestFileQueueRecord (int amount) throws SQLException {

        List<FileQueueRecord> fileQueueRecord;
        Connection connection = Hilo.connection();
        final int previousIsolationLevel = connection.getTransactionIsolation();
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);

            fileQueueRecord = this.findAllWherePending(amount);

            if (fileQueueRecord.isEmpty()) {
                return fileQueueRecord;
            }

            Long[] idList = fileQueueRecord.stream().map(FileQueueRecord::getId).toArray(Long[]::new);
            for (Long id : idList) {
                if (!this.updateFileQueueStatusById(id)) {
                    //TODO didn't work but didnt throw an exception
                }
            }
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(previousIsolationLevel);
        }
        return fileQueueRecord;
    }
}
