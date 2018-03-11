/*
 * This file is generated by jOOQ.
 */
package com.diluv.hilo.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import com.diluv.hilo.models.tables.Project;
import com.diluv.hilo.models.tables.ProjectFile;
import com.diluv.hilo.models.tables.ProjectFileProcessing;

/**
 * This class is generated by jOOQ.
 */
@Generated(value = { "http://www.jooq.org", "jOOQ version:3.9.3" }, comments = "This class is generated by jOOQ")
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Diluv extends SchemaImpl {

    private static final long serialVersionUID = 448634714;

    /**
     * The reference instance of <code>diluv</code>
     */
    public static final Diluv DILUV = new Diluv();

    /**
     * The table <code>diluv.PROJECT</code>.
     */
    public final Project PROJECT = com.diluv.hilo.models.tables.Project.PROJECT;

    /**
     * The table <code>diluv.PROJECT_FILE</code>.
     */
    public final ProjectFile PROJECT_FILE = com.diluv.hilo.models.tables.ProjectFile.PROJECT_FILE;

    /**
     * The table <code>diluv.PROJECT_FILE_PROCESSING</code>.
     */
    public final ProjectFileProcessing PROJECT_FILE_PROCESSING = com.diluv.hilo.models.tables.ProjectFileProcessing.PROJECT_FILE_PROCESSING;

    /**
     * No further instances allowed
     */
    private Diluv () {

        super("diluv", null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog () {

        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables () {

        final List result = new ArrayList();
        result.addAll(this.getTables0());
        return result;
    }

    private final List<Table<?>> getTables0 () {

        return Arrays.<Table<?>> asList(Project.PROJECT, ProjectFile.PROJECT_FILE, ProjectFileProcessing.PROJECT_FILE_PROCESSING);
    }
}
