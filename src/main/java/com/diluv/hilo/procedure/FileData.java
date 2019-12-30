package com.diluv.hilo.procedure;

import java.util.Date;

public class FileData {

    /**
     * An internally unique identifier for the file.
     */
    public long id;

    /**
     * The display name of the file.
     */
    public String name;

    /**
     * A MD5 hash of the file.
     */
    public String md5;

    /**
     * A SHA 256 hash of the file.
     */
    public String sha256;

    /**
     * A SHA 512 hash of the file.
     */
    public String sha512;

    /**
     * A crc32 hash of the file.
     */
    public String crc32;

    /**
     * The size of the file in bytes.
     */
    public long size;

    /**
     * The change log text for the file.
     */
    public String changelog;

    /**
     * The date the file was initially uploaded.
     */
    public Date createdAt;

    /**
     * The date the file was last modified.
     */
    public Date updatedAt;

    /**
     * Whether or not the file has been reviewed.
     */
    public boolean reviewed;

    /**
     * The id of the project that this file belongs to.
     */
    public long projectId;

    /**
     * The id of the user who uploaded the file.
     */
    public long uploaderId;
}