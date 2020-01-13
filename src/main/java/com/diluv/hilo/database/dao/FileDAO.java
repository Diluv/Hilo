package com.diluv.hilo.database.dao;

import java.sql.SQLException;
import java.util.List;

import com.diluv.hilo.database.records.FileQueueRecord;

public interface FileDAO {
    List<FileQueueRecord> findAllWherePending (int amount);

    boolean updateFileQueueStatusById (long id) throws SQLException;

    List<FileQueueRecord> getLatestFileQueueRecord (int amount) throws SQLException;
}
