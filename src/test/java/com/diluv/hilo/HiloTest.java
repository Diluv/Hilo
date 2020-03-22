package com.diluv.hilo;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.time.Duration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.diluv.clamchowder.ClamClient;
import com.diluv.confluencia.Confluencia;
import com.diluv.confluencia.database.record.FileProcessingStatus;
import com.diluv.confluencia.database.record.ProjectFileRecord;
import com.diluv.hilo.utils.Constants;

@Testcontainers
public class HiloTest {

    static final MariaDBContainer MYSQL_CONTAINER;
    static final GenericContainer CLAMAV;

    static {
        MYSQL_CONTAINER = new MariaDBContainer<>();
        MYSQL_CONTAINER.start();

        CLAMAV = new GenericContainer<>("diluv/clamav")
            .withExposedPorts(3310)
            .waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)));
        CLAMAV.start();
    }

    @BeforeAll
    public static void start () {

        try {
            FileUtils.deleteDirectory(new File(Constants.NODECDN_FOLDER));

            Main.CLEAN_UP = false;
            Main.CLAM = new ClamClient(CLAMAV.getContainerIpAddress(), CLAMAV.getMappedPort(3310), Constants.CLAM_TIMEOUT, Constants.CLAM_SIZE, Constants.CLAM_READ_BUFFER);

            Security.addProvider(new BouncyCastleProvider());

            Confluencia.init(MYSQL_CONTAINER.getJdbcUrl(), MYSQL_CONTAINER.getUsername(), MYSQL_CONTAINER.getPassword(), false);
            Assertions.assertTrue(Main.CLAM.ping());

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test () {

        Thread one = new Thread(() -> {
            try {
                while (Main.RUNNING) {
                    Thread.sleep(1000);
                    final List<ProjectFileRecord> running = Main.DATABASE.fileDAO.findAllWhereStatusAndLimit(FileProcessingStatus.RUNNING, 5);
                    final List<ProjectFileRecord> pending = Main.DATABASE.fileDAO.findAllWhereStatusAndLimit(FileProcessingStatus.PENDING, 5);
                    if (running.size() == 0 && pending.size() == 0) {
                        Main.RUNNING = false;
                    }
                }
            }
            catch (InterruptedException e) {
                Assertions.assertNull(e);
            }
        });

        one.start();

        Main.HILO.start();
    }
}
