package com.diluv.hilo;

import com.diluv.confluencia.Confluencia;
import com.diluv.confluencia.database.record.FileProcessingStatus;
import com.diluv.confluencia.database.record.NodeCDNCommitsEntity;
import com.diluv.confluencia.database.record.ProjectFilesEntity;
import com.diluv.hilo.procedure.ProcessingProcedure;
import com.diluv.hilo.utils.Constants;
import com.diluv.nodecdn.NodeCDN;
import com.diluv.nodecdn.request.RequestCommit;
import com.diluv.nodecdn.response.Response;
import com.diluv.nodecdn.response.commits.head.ResponseCommitsHead;
import com.diluv.schoomp.Webhook;
import com.diluv.schoomp.message.Message;

import org.apache.commons.lang3.concurrent.TimedSemaphore;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Instances of Hilo are responsible for polling the database for newly created files. When new
 * files are found they are added to a processing pool that applies a processing procedure to
 * them.
 */
public class Hilo {

    /**
     * The thread pool for file processing.
     */
    private final ThreadPoolExecutor processingExecutor;

    /**
     * The procedure to use when processing files.
     */
    private final ProcessingProcedure procedure;

    private Webhook webhook;
    private NodeCDN nodeCDN;
    private long lastTime;

    /**
     * Constructs a new hilo instance.
     *
     * @param threadCount The amount of file processing threads to use. Each thread will handle
     *     one file.
     * @param procedure The procedure to use when processing files.
     */
    public Hilo (int threadCount, ProcessingProcedure procedure) {

        this.processingExecutor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.procedure = procedure;
    }

    public void start () {

        if (Constants.WEBHOOK_URL != null) {
            this.webhook = new Webhook(Constants.WEBHOOK_URL, "Diluv Service - Hilo");
        }

        if (Constants.NODECDN_USERNAME != null) {
            this.nodeCDN = new NodeCDN(Constants.NODECDN_USERNAME, Constants.NODECDN_PASSWORD);
        }

        Confluencia.getTransaction(session -> {
            if (!Confluencia.FILE.updateStatusByStatus(session, FileProcessingStatus.PENDING, FileProcessingStatus.RUNNING)) {

                Main.LOGGER.error("Failed to reset status.");
            }
        });

        this.poll(new TimedSemaphore(15, TimeUnit.SECONDS, 1));
    }

    private void poll (TimedSemaphore semaphore) {

        try {
            while (true) {
                semaphore.acquire();
                final List<ProjectFilesEntity> projectFiles = Confluencia.getTransaction(session -> {
                    return Confluencia.FILE.getLatestFiles(session, this.getOpenProcessingThreads());
                });

                if (!projectFiles.isEmpty()) {
                    Main.LOGGER.info("Enqueued {} new files.", projectFiles.size());
                    projectFiles.forEach(file -> this.processingExecutor.submit(new TaskProcessFile(file, this.procedure)));
                    updateNodeCDN();
                }
                else {
                    boolean callNodeCDN = Confluencia.getTransaction(session -> {
                        if (Confluencia.MISC.existsImagesForRelease(session)) {
                            return true;
                        }
                        List<ProjectFilesEntity> pending = Confluencia.FILE.findAllWhereStatusAndLimit(session, FileProcessingStatus.SUCCESS, 1);
                        return !pending.isEmpty();
                    });

                    if (callNodeCDN) {
                        updateNodeCDN();
                    }
                }

                if (Constants.isDevelopment()) {
                    boolean end = Confluencia.getTransaction(session -> {
                        List<ProjectFilesEntity> pending = Confluencia.FILE.findAllWhereStatusAndLimit(session, FileProcessingStatus.PENDING, 1);
                        List<ProjectFilesEntity> running = Confluencia.FILE.findAllWhereStatusAndLimit(session, FileProcessingStatus.RUNNING, 1);
                        return pending.isEmpty() && running.isEmpty();
                    });
                    if (end) {
                        break;
                    }
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateNodeCDN () throws RuntimeException {

        if (this.nodeCDN == null) {
            return;
        }

        if (System.currentTimeMillis() - this.lastTime < TimeUnit.MINUTES.toMillis(5)) {
            return;
        }
        this.lastTime = System.currentTimeMillis();

        String uuid = UUID.randomUUID().toString();
        NodeCDNCommitsEntity commit = new NodeCDNCommitsEntity();
        commit.setHash(uuid);

        String message = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).toString();

        RequestCommit request = new RequestCommit(message, Constants.getNodeCDNWebhookUrl(uuid), true, true);
        Response<ResponseCommitsHead> response = this.nodeCDN.getNodeCDNService().postCommit(request);

        if (response.isSuccess()) {
            Confluencia.getTransaction(session -> {
                session.save(commit);
            });
        }
        else {
            throw new RuntimeException("Request Unsuccessful");
        }

        try {
            Message discordMessage = new Message();
            discordMessage.setContent("Pushed to NodeCDN " + Instant.now().toString());
            discordMessage.setUsername("Hilo");
            this.webhook.sendMessage(discordMessage);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOpenProcessingThreads () {

        return this.processingExecutor.getMaximumPoolSize() - this.processingExecutor.getActiveCount();
    }
}