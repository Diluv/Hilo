package com.diluv.hilo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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
import com.diluv.hilo.utils.FileUtil;

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
            FileUtils.deleteDirectory(new File(Constants.PROCESSING_FOLDER));
            FileUtils.deleteDirectory(new File(Constants.NODECDN_FOLDER));

            Main.CLAM = new ClamClient(CLAMAV.getContainerIpAddress(), CLAMAV.getMappedPort(3310), Constants.CLAM_TIMEOUT, Constants.CLAM_SIZE, Constants.CLAM_READ_BUFFER);
            Assertions.assertTrue(Main.CLAM.ping());

            Security.addProvider(new BouncyCastleProvider());

            Confluencia.init(MYSQL_CONTAINER.getJdbcUrl(), MYSQL_CONTAINER.getUsername(), MYSQL_CONTAINER.getPassword(), false);

            File virus = new File(Constants.PROCESSING_FOLDER, "minecraft/mods/1/6/malware.txt");
            virus.getParentFile().mkdirs();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(virus), Charset.defaultCharset()))) {
                writer.write("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            List<ProjectFileRecord> files = Main.DATABASE.fileDAO.findAllWhereStatusAndLimit(FileProcessingStatus.PENDING, 100);
            for (ProjectFileRecord record : files) {
                InputStream io = HiloTest.class.getResourceAsStream("/testfiles/" + record.getName());
                if (io != null) {
                    File output = FileUtil.getProcessingLocation(record);
                    output.getParentFile().mkdirs();
                    try {
                        FileUtils.copyInputStreamToFile(io, output);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test () {

        new Thread(() -> {
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
        }).start();

        Main.HILO.start();

        this.check(FileProcessingStatus.PENDING, 0);
        this.check(FileProcessingStatus.RUNNING, 0);
        this.check(FileProcessingStatus.SUCCESS, 5);
        this.check(FileProcessingStatus.FAILED_UNSPECIFIED, 0);
        this.check(FileProcessingStatus.FAILED_INTERNAL_SERVER_ERROR, 0);
        this.check(FileProcessingStatus.FAILED_MALWARE_DETECTED, 2);
        this.check(FileProcessingStatus.FAILED_MALWARE_SCAN_TIMEOUT, 0);
        this.check(FileProcessingStatus.FAILED_INVALID_FILE, 0);
    }

    public void check (FileProcessingStatus status, int expected) {

        final List<ProjectFileRecord> files = Main.DATABASE.fileDAO.findAllWhereStatusAndLimit(status, 100);
        Assertions.assertEquals(expected, files.size());
    }
}
