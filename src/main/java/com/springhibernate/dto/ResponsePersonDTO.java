package com.springhibernate.dto;

import java.io.File;

public class ResponsePersonDTO {

    private String name;
    private String surname;
    private File convertedFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public File getConvertedFile() {
        return convertedFile;
    }

    public void setConvertedFile(File convertedFile) {
        this.convertedFile = convertedFile;
    }
}
