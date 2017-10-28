package com.diluv.hilo.process;

import com.diluv.hilo.models.tables.records.ProjectFileRecord;
import com.diluv.inquisitor.Inquisitor;
import com.diluv.inquisitor.report.IReport;
import net.diluv.inquisitor.clamav.EngineClamAV;

import java.io.File;
import java.sql.Connection;
import java.util.List;

/**
 * Runs the Inquisitor process to scan files for virus'
 */
public class ProcessInquisitor implements IProcess {

    public Inquisitor inquisitor;

    public ProcessInquisitor() {
        this.inquisitor = new Inquisitor();
        this.inquisitor.addEngine(new EngineClamAV(System.getenv("clamAVHost"), Integer.parseInt(System.getenv("clamAVPort")), 3600));
    }

    @Override
    public String getProcessName() {
        return "Inquisitor";
    }

    @Override
    public boolean processFile(File preReleaseFile, ProjectFileRecord projectFile, Connection conn, StringBuilder logger) {
        try {
            List<IReport> reportList = this.inquisitor.scanFile(preReleaseFile);

            for (IReport report : reportList) {
                logger.append(String.format("%s (%s)", report.getTitle(), report.getEngineName())).append("\n");
                logger.append(report.getDescription()).append("\n").append("\n");
            }

            return reportList.isEmpty();
        } catch (Exception e) {
            logger.append(e.getLocalizedMessage());
            return false;
        }
    }
}
