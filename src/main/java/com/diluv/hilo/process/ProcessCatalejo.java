package com.diluv.hilo.process;

import static com.diluv.hilo.models.Tables.PROJECT_FILE;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.diluv.catalejo.Catalejo;
import com.diluv.hilo.models.tables.records.ProjectFileRecord;

/**
 * Runs the process for Catalejo, it gets the SHA-256 of the file
 */
public class ProcessCatalejo implements IProcess {

    private final Catalejo catalejo;

    public ProcessCatalejo () {

        this.catalejo = new Catalejo();
        this.catalejo.add(Catalejo.SHA_512_READER);
    }

    @Override
    public String getProcessName () {

        return "Catalejo";
    }

    @Override
    public boolean processFile (File preReleaseFile, ProjectFileRecord projectFile, Connection conn, StringBuilder logger) {

        final Map<String, Object> meta = new HashMap<>();
        try {
            this.catalejo.readFileMeta(meta, preReleaseFile);
        }
        catch (final Exception e) {
            logger.append("SHA 512 exception\n");
            logger.append(e.toString());
            return false;
        }

        final Object sha512 = meta.get("SHA-512");
        if (sha512 == null) {
            logger.append("SHA 512 is null, Internal Error\n");
            return false;
        }
        final DSLContext transaction = DSL.using(conn, SQLDialect.MYSQL);

        transaction.update(PROJECT_FILE).set(PROJECT_FILE.SHA512, (String) sha512).where(PROJECT_FILE.ID.eq(projectFile.getId())).execute();
        return true;
    }
}
