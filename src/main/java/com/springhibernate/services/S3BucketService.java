package com.springhibernate.services;

import com.springhibernate.dto.PersonDTO;
import com.springhibernate.dto.ResponsePersonDTO;
import com.springhibernate.entity.FileObject;
import com.springhibernate.entity.Person;
import com.springhibernate.entity.PersonName;
import com.springhibernate.repository.PersonRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.IoUtils;
import software.amazon.awssdk.utils.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class S3BucketService {

    private static final String bucketName = "prashant-ttn";
    private static final Region region = Region.US_EAST_1;
    private static final String keyName = "file1";
    @Autowired
    private PersonRepository personRepository;

    private S3Client getClient() {
        return S3Client.builder().region(region).build();
    }

    private void listBucketObjects(String bucket) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).build();
        ListObjectsV2Response listObjectsResponse = getClient().listObjectsV2(listObjectsRequest);
        if (CollectionUtils.isNotEmpty(listObjectsResponse.contents())) {
            listObjectsResponse.contents().forEach(
                    content -> System.out.println(" Key: " + content.key() + " size = " + content.size()));
        }
    }

    private void listBuckets() {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = getClient().listBuckets(listBucketsRequest);
        listBucketsResponse.buckets().forEach(bucket -> System.out.println("Bucket: " + bucket.name()));
    }

    private void createBucket(String bucket, Region region) {
        CreateBucketConfiguration createBucketConfiguration = CreateBucketConfiguration.builder()
                                                                                       .locationConstraint(region.id())
                                                                                       .build();
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                                                                     .bucket(bucket)
                                                                     .createBucketConfiguration(
                                                                             createBucketConfiguration)
                                                                     .build();
        CreateBucketResponse createBucketResponse = getClient().createBucket(createBucketRequest);
        System.out.println(createBucketResponse.toString());
    }

    private void deleteBucket(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
        DeleteBucketResponse deleteBucketResponse = getClient().deleteBucket(deleteBucketRequest);
        System.out.println(deleteBucketResponse.toString());
    }

    public void savePersonInfo(PersonDTO personDTO) {
        try {
            PersonName personName = new PersonName();
            personName.setName(personDTO.getName());
            personName.setSurname(personDTO.getSurname());

            File fileTemp = createFileFromMultipartFile(personDTO.getFile());

            FileObject fileObject = new FileObject();
            fileObject.setFile(fileTemp);

            Person person = new Person();
            person.setPersonName(personName);
            person.setS3Object(fileObject);

            personRepository.save(person);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createFileFromMultipartFile(MultipartFile multipartFile)
    throws IOException {
        String prefix = UUID.randomUUID().toString();
        String type = multipartFile.getContentType();
        String suffix = "";
        if (StringUtils.isNotBlank(type)) {
            suffix = "." + type.split("/")[1];
        }
        File fileTemp = File.createTempFile(prefix, suffix);
        FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);
        FileInputStream fileInputStream = (FileInputStream) multipartFile.getInputStream();
        IoUtils.copy(fileInputStream, fileOutputStream);
        return fileTemp;
    }

    public Boolean putFileInBucket(File file) {
        Boolean result = Boolean.FALSE;
        S3Client s3Client = getClient();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
                                                            .key(file.getName()).build();
        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file.toPath());
        //System.out.println(putObjectResponse.toString());

        try {
            Path fileToDeletePath = Paths.get(file.getPath());
            Files.deleteIfExists(fileToDeletePath);
            result = Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public File getFileFromBucket(String keyName, String tempFilePath) {
        Path path = Paths.get(tempFilePath);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(keyName).build();
        GetObjectResponse getObjectResponse = getClient()
                .getObject(getObjectRequest, ResponseTransformer.toFile(path));
        //System.out.println(getObjectResponse.toString());
        return new File(tempFilePath);
    }

    public ResponsePersonDTO getPersonInfo(Long id) {
        Person person = personRepository.getOne(id);
        ResponsePersonDTO responsePersonDTO = new ResponsePersonDTO();
        responsePersonDTO.setName(person.getPersonName().getName());
        responsePersonDTO.setSurname(person.getPersonName().getSurname());
        responsePersonDTO.setConvertedFile(person.getS3Object().getFile());

        return responsePersonDTO;
    }

}
