package com.springhibernate.controllers;

import com.springhibernate.dto.PersonDTO;
import com.springhibernate.dto.ResponsePersonDTO;
import com.springhibernate.services.S3BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class TestController {

    @Autowired
    private S3BucketService s3BucketService;

    @GetMapping("/")
    public String testMethod() {
        return "Working Fine Prashant";
    }

    @PostMapping("/save")
    public String savePersonInfo(@ModelAttribute PersonDTO personDTO) {
        s3BucketService.savePersonInfo(personDTO);
        return "Working Fine";
    }

    @GetMapping("/fetch/{id}")
    public String getPersonInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id) {
        ResponsePersonDTO responsePersonDTO = s3BucketService.getPersonInfo(id);
        try {
            File file1 = responsePersonDTO.getConvertedFile();
            if (file1 != null) {
                FileInputStream inputStream = new FileInputStream(file1);
                OutputStream outStream = response.getOutputStream();
                ServletContext context = request.getServletContext();

                String mimeType = context.getMimeType(file1.getPath());
                if (mimeType == null) {
                    // set to binary type if MIME mapping not found
                    mimeType = "application/octet-stream";
                }

                // set content attributes for the response
                response.setContentType(mimeType);
                response.setContentLength((int) file1.length());

                // set headers for the response
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"", file1.getName());
                response.setHeader(headerKey, headerValue);

                FileCopyUtils.copy(inputStream, outStream);

                Path fileToDeletePath = Paths.get(file1.getPath());
                Files.deleteIfExists(fileToDeletePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Working Fine";
    }
}
