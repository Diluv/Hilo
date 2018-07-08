package com.diluv.hilo;

import com.diluv.hilo.db.models.tables.records.ProjectFileRecord;
import com.diluv.hilo.process.ProcessQueue;
import com.diluv.hilo.process.modules.ProcessCatalejo;
import com.diluv.hilo.process.modules.ProcessInquisitor;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.TransactionalCallable;
import org.jooq.impl.DSL;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.diluv.hilo.db.models.Tables.PROJECT_FILE;

public class Hilo {

    private static Hilo INSTANCE;

    private static final ExecutorService fileExecutor = createExecutor("Hilo Processing");

    private boolean running;

    /**
     * Connects to the database and starts processing the database
     */
    public void start() {
        String host = System.getenv("dbHost");
        String port = System.getenv("dbPort");
        String database = System.getenv("database");
        String user = System.getenv("dbUsername");
        String password = System.getenv("dbPassword");

        //TODO Fix input strings
        String url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);

        try {
            final Connection conn = DriverManager.getConnection(url, user, password);
            ProcessQueue processQueue = new ProcessQueue(DSL.using(conn, SQLDialect.MYSQL));
            processQueue.add(new ProcessCatalejo());
            processQueue.add(new ProcessInquisitor());

            this.running = true;
            while (running) {
                try {
                    TransactionalCallable<ProjectFileRecord> transactional = configuration -> {
                        Record1<Long> id = DSL.using(configuration).select(PROJECT_FILE.ID)
                                .from(PROJECT_FILE)
                                .where(PROJECT_FILE.PUBLIC.eq(false).and(PROJECT_FILE.PROCESSING.eq(false).and(PROJECT_FILE.REVIEW_NEEDED.eq(false))))
                                .orderBy(PROJECT_FILE.CREATED_AT.desc())
                                .fetchOne();

                        if (id == null)
                            return null;

                        long projectFileId = id.get(PROJECT_FILE.ID);

                        DSL.using(configuration).update(PROJECT_FILE)
                                .set(PROJECT_FILE.PROCESSING, true)
                                .where(PROJECT_FILE.ID.eq((projectFileId)))
                                .execute();

                        return DSL.using(configuration).selectFrom(PROJECT_FILE)
                                .where(PROJECT_FILE.ID.eq(projectFileId))
                                .fetchOne();
                    };

                    ProjectFileRecord dbProject = DSL.using(conn, SQLDialect.MYSQL).transactionResult(transactional);
                    if (dbProject != null) {
                        fileExecutor.execute(() -> processQueue.process(conn, dbProject));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO Log properly to database/discord
                    this.running = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates an 8 thread executor to run the file processing in parallel as a daemon
     *
     * @param threadName The name of the thread pool
     * @return ExecutorService of 8 thread pool with the threads running as a daemon
     */
    public static ExecutorService createExecutor(String threadName) {
        return Executors.newFixedThreadPool(8,
                r -> {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setName(threadName);
                    t.setDaemon(true);
                    return t;
                });

    }

    public static void main(String[] args) {
        INSTANCE = new Hilo();
        INSTANCE.start();
    }
}
