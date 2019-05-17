package com.springhibernate.entity;

import com.springhibernate.constants.ApplicationConstant;
import com.springhibernate.services.Holders;
import com.springhibernate.services.S3BucketService;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.File;

@Converter(autoApply = true)
public class S3FileConverter implements AttributeConverter<FileObject, String> {

    @Override
    public String convertToDatabaseColumn(FileObject attribute) {
        S3BucketService s3BucketService = Holders.getBean(S3BucketService.class);
        String keyName = null;
        if (attribute != null && attribute.getFile() != null && s3BucketService != null) {
            Boolean result = s3BucketService.putFileInBucket(attribute.getFile());
            if (result) {
                keyName = attribute.getFile().getName();
                System.out.println("File Created successfully");
            } else {
                System.out.println("Failed to create file");
            }

        } else {
            System.out.println("FileObject is null");
        }
        System.out.println("Uploading file to s3........");
        return keyName;
    }

    @Override
    public FileObject convertToEntityAttribute(String dbData) {
        S3BucketService s3BucketService = Holders.getBean(S3BucketService.class);
        String tempFilePath = ApplicationConstant.fileDirectoryPath + dbData;

        FileObject fileObject = new FileObject();
        File file = s3BucketService.getFileFromBucket(dbData, tempFilePath);
        if (file != null) {
            fileObject.setFile(file);
        }
        System.out.println("downloading file from s3........");

        return fileObject;
    }

}
