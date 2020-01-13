package com.diluv.hilo.database.dao;

import com.diluv.hilo.database.records.FileQueueRecord;

import java.sql.SQLException;
import java.util.List;

public interface FileDAO {
    List<FileQueueRecord> findAllWherePending (int amount);

    boolean updateFileQueueStatusById (long id) throws SQLException;

    List<FileQueueRecord> getLatestFileQueueRecord (int amount) throws SQLException;
}
