package com.diluv.hilo.process;

import com.diluv.catalejo.Catalejo;
import com.diluv.hilo.models.tables.records.ProjectFileRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static com.diluv.hilo.models.Tables.PROJECT_FILE;

/**
 * Runs the process for Catalejo, it gets the SHA-256 of the file
 */
public class ProcessCatalejo implements IProcess {

    public Catalejo catalejo;

    public ProcessCatalejo() {
        this.catalejo = new Catalejo();
        this.catalejo.add(Catalejo.SHA_256_READER);
    }

    @Override
    public String getProcessName() {
        return "Catalejo";
    }

    @Override
    public boolean processFile(File preReleaseFile, ProjectFileRecord projectFile, Connection conn, StringBuilder logger) {
        Map<String, Object> meta = new HashMap<>();
        this.catalejo.readFileMeta(meta, preReleaseFile);

        Object sha256 = meta.get("SHA-256");
        if (sha256 == null) {
            logger.append("SHA 256 is null, Internal Error\n");
            return false;
        }
        DSLContext transaction = DSL.using(conn, SQLDialect.MYSQL);

        transaction.update(PROJECT_FILE)
                .set(PROJECT_FILE.SHA256, (String) sha256)
                .where(PROJECT_FILE.ID.eq(projectFile.getId()))
                .execute();
        return true;
    }
}
