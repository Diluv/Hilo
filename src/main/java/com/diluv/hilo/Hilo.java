package com.diluv.hilo;

import static com.diluv.hilo.models.Tables.PROJECT_FILE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.TransactionalCallable;
import org.jooq.impl.DSL;

import com.diluv.hilo.models.tables.records.ProjectFileRecord;
import com.diluv.hilo.process.ProcessCatalejo;
import com.diluv.hilo.process.ProcessInquisitor;
import com.diluv.hilo.process.ProcessQueue;

public class Hilo {

    private static final Hilo INSTANCE = new Hilo();

    public static final Logger LOG = LogManager.getLogger("Hilo");

    private static final ExecutorService fileExecutor = createExecutor("Hilo Processing");

    /**
     * Connects to the database and starts processing the database
     */
    public void start () {

        final String host = System.getenv("dbHost");
        final String port = System.getenv("dbPort");
        final String database = System.getenv("database");
        final String user = System.getenv("dbUsername");
        final String password = System.getenv("dbPassword");

        // TODO Fix input strings
        final String url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);

        try {
            final Connection conn = DriverManager.getConnection(url, user, password);
            final ProcessQueue processQueue = new ProcessQueue(DSL.using(conn, SQLDialect.MYSQL));
            processQueue.add(new ProcessCatalejo());
            processQueue.add(new ProcessInquisitor());

            boolean running = true;
            while (running) {
                try {
                    Thread.sleep(1);
                }
                catch (final InterruptedException e) {
                    LOG.trace("Sleep Interrupted", e);
                }
                try {
                    final TransactionalCallable<ProjectFileRecord> transactional = configuration -> {
                        final Record1<Long> id = DSL.using(configuration).select(PROJECT_FILE.ID).from(PROJECT_FILE).where(PROJECT_FILE.PUBLIC.eq(false).and(PROJECT_FILE.PROCESSING.eq(false).and(PROJECT_FILE.REVIEW_NEEDED.eq(false)))).orderBy(PROJECT_FILE.CREATED_AT.desc()).fetchOne();

                        if (id == null) {
                            return null;
                        }

                        final long projectFileId = id.get(PROJECT_FILE.ID);

                        DSL.using(configuration).update(PROJECT_FILE).set(PROJECT_FILE.PROCESSING, true).where(PROJECT_FILE.ID.eq(projectFileId)).execute();

                        return DSL.using(configuration).selectFrom(PROJECT_FILE).where(PROJECT_FILE.ID.eq(projectFileId)).fetchOne();
                    };

                    final ProjectFileRecord dbProject = DSL.using(conn, SQLDialect.MYSQL).transactionResult(transactional);
                    if (dbProject != null) {
                        fileExecutor.execute( () -> processQueue.process(conn, dbProject));
                    }
                }
                catch (final Exception e) {

                    LOG.trace("There was an error", e);
                    running = false;
                }
            }
        }
        catch (final SQLException e) {
            LOG.trace("There was an error", e);
        }

    }

    /**
     * Creates an 8 thread executor to run the file processing in parallel as a
     * daemon
     *
     * @param threadName The name of the thread pool
     * @return ExecutorService of 8 thread pool with the threads running as a
     *         daemon
     */
    public static ExecutorService createExecutor (String threadName) {

        return Executors.newFixedThreadPool(8, r -> {
            final Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName(threadName);
            t.setDaemon(true);
            return t;
        });

    }

    public static void main (String[] args) {

        INSTANCE.start();
    }
}
