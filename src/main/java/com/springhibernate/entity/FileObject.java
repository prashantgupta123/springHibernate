package com.springhibernate.entity;

import java.io.File;
import java.io.Serializable;

public class FileObject implements Serializable {

    private String name;
    private String type;
    private Long size;
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
