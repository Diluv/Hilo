package com.diluv.hilo.database.records;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FileQueueRecord {
    private final long id;
    private final String name;
    private final String changelog;
    private final long createdAt;
    private final long projectId;
    private final long userId;

    public FileQueueRecord (ResultSet rs) throws SQLException {

        this.id = rs.getLong("id");
        this.name = rs.getString("name");
        this.changelog = rs.getString("changelog");
        this.createdAt = rs.getTimestamp("created_at").getTime();
        this.projectId = rs.getLong("project_id");
        this.userId = rs.getLong("user_id");
    }

    public long getId () {

        return this.id;
    }

    public String getName () {

        return this.name;
    }

    public String getChangelog () {

        return this.changelog;
    }

    public long getCreatedAt () {

        return this.createdAt;
    }

    public long getProjectId () {

        return this.projectId;
    }

    public long getUserId () {

        return this.userId;
    }
}
