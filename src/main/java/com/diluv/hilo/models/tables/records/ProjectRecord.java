/*
 * This file is generated by jOOQ.
 */
package com.diluv.hilo.models.tables.records;


import com.diluv.hilo.models.tables.Project;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record14;
import org.jooq.Row14;
import org.jooq.impl.UpdatableRecordImpl;

import javax.annotation.Generated;
import java.sql.Timestamp;


/**
 * This class is generated by jOOQ.
 */
@Generated(
        value = {
                "http://www.jooq.org",
                "jOOQ version:3.9.3"
        },
        comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProjectRecord extends UpdatableRecordImpl<ProjectRecord> implements Record14<Long, String, String, String, String, String, String, Long, Boolean, Boolean, Timestamp, Timestamp, Long, Long> {

    private static final long serialVersionUID = 530023422;

    /**
     * Setter for <code>diluv.PROJECT.ID</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.ID</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>diluv.PROJECT.NAME</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.NAME</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>diluv.PROJECT.SHORT_DESCRIPTION</code>.
     */
    public void setShortDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.SHORT_DESCRIPTION</code>.
     */
    public String getShortDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>diluv.PROJECT.DESCRIPTION</code>.
     */
    public void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.DESCRIPTION</code>.
     */
    public String getDescription() {
        return (String) get(3);
    }

    /**
     * Setter for <code>diluv.PROJECT.DESCRIPTION_TYPE</code>.
     */
    public void setDescriptionType(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.DESCRIPTION_TYPE</code>.
     */
    public String getDescriptionType() {
        return (String) get(4);
    }

    /**
     * Setter for <code>diluv.PROJECT.SLUG</code>.
     */
    public void setSlug(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.SLUG</code>.
     */
    public String getSlug() {
        return (String) get(5);
    }

    /**
     * Setter for <code>diluv.PROJECT.LOGO</code>.
     */
    public void setLogo(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.LOGO</code>.
     */
    public String getLogo() {
        return (String) get(6);
    }

    /**
     * Setter for <code>diluv.PROJECT.TOTAL_DOWNLOADS</code>.
     */
    public void setTotalDownloads(Long value) {
        set(7, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.TOTAL_DOWNLOADS</code>.
     */
    public Long getTotalDownloads() {
        return (Long) get(7);
    }

    /**
     * Setter for <code>diluv.PROJECT.NEW_PROJECT</code>.
     */
    public void setNewProject(Boolean value) {
        set(8, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.NEW_PROJECT</code>.
     */
    public Boolean getNewProject() {
        return (Boolean) get(8);
    }

    /**
     * Setter for <code>diluv.PROJECT.DELETED</code>.
     */
    public void setDeleted(Boolean value) {
        set(9, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.DELETED</code>.
     */
    public Boolean getDeleted() {
        return (Boolean) get(9);
    }

    /**
     * Setter for <code>diluv.PROJECT.UPDATED_AT</code>.
     */
    public void setUpdatedAt(Timestamp value) {
        set(10, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.UPDATED_AT</code>.
     */
    public Timestamp getUpdatedAt() {
        return (Timestamp) get(10);
    }

    /**
     * Setter for <code>diluv.PROJECT.CREATED_AT</code>.
     */
    public void setCreatedAt(Timestamp value) {
        set(11, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.CREATED_AT</code>.
     */
    public Timestamp getCreatedAt() {
        return (Timestamp) get(11);
    }

    /**
     * Setter for <code>diluv.PROJECT.PROJECT_TYPE_ID</code>.
     */
    public void setProjectTypeId(Long value) {
        set(12, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.PROJECT_TYPE_ID</code>.
     */
    public Long getProjectTypeId() {
        return (Long) get(12);
    }

    /**
     * Setter for <code>diluv.PROJECT.USER_ID</code>.
     */
    public void setUserId(Long value) {
        set(13, value);
    }

    /**
     * Getter for <code>diluv.PROJECT.USER_ID</code>.
     */
    public Long getUserId() {
        return (Long) get(13);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record14 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row14<Long, String, String, String, String, String, String, Long, Boolean, Boolean, Timestamp, Timestamp, Long, Long> fieldsRow() {
        return (Row14) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row14<Long, String, String, String, String, String, String, Long, Boolean, Boolean, Timestamp, Timestamp, Long, Long> valuesRow() {
        return (Row14) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Project.PROJECT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Project.PROJECT.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Project.PROJECT.SHORT_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Project.PROJECT.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Project.PROJECT.DESCRIPTION_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Project.PROJECT.SLUG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Project.PROJECT.LOGO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field8() {
        return Project.PROJECT.TOTAL_DOWNLOADS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field9() {
        return Project.PROJECT.NEW_PROJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field10() {
        return Project.PROJECT.DELETED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field11() {
        return Project.PROJECT.UPDATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field12() {
        return Project.PROJECT.CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field13() {
        return Project.PROJECT.PROJECT_TYPE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field14() {
        return Project.PROJECT.USER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getShortDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getDescriptionType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getSlug();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getLogo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value8() {
        return getTotalDownloads();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value9() {
        return getNewProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value10() {
        return getDeleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value11() {
        return getUpdatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value12() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value13() {
        return getProjectTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value14() {
        return getUserId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value3(String value) {
        setShortDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value4(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value5(String value) {
        setDescriptionType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value6(String value) {
        setSlug(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value7(String value) {
        setLogo(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value8(Long value) {
        setTotalDownloads(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value9(Boolean value) {
        setNewProject(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value10(Boolean value) {
        setDeleted(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value11(Timestamp value) {
        setUpdatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value12(Timestamp value) {
        setCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value13(Long value) {
        setProjectTypeId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord value14(Long value) {
        setUserId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectRecord values(Long value1, String value2, String value3, String value4, String value5, String value6, String value7, Long value8, Boolean value9, Boolean value10, Timestamp value11, Timestamp value12, Long value13, Long value14) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProjectRecord
     */
    public ProjectRecord() {
        super(Project.PROJECT);
    }

    /**
     * Create a detached, initialised ProjectRecord
     */
    public ProjectRecord(Long id, String name, String shortDescription, String description, String descriptionType, String slug, String logo, Long totalDownloads, Boolean newProject, Boolean deleted, Timestamp updatedAt, Timestamp createdAt, Long projectTypeId, Long userId) {
        super(Project.PROJECT);

        set(0, id);
        set(1, name);
        set(2, shortDescription);
        set(3, description);
        set(4, descriptionType);
        set(5, slug);
        set(6, logo);
        set(7, totalDownloads);
        set(8, newProject);
        set(9, deleted);
        set(10, updatedAt);
        set(11, createdAt);
        set(12, projectTypeId);
        set(13, userId);
    }
}
