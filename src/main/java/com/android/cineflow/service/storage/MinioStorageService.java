package com.android.cineflow.service.storage;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements IStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                log.info("Bucket '{}' does not exist. Creating it...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                
                // Configure read-only access policy for the bucket so clients can access media directly
                String policy = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": \"*\",\n" +
                        "      \"Action\": [\"s3:GetObject\"],\n" +
                        "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
                log.info("Bucket '{}' created and public read-only policy applied.", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // Prefix with folder name (e.g. "films/uuid.mp4", "shorts/uuid.mp4")
            String cleanFolder = (folder == null || folder.trim().isEmpty()) ? "" : folder.trim() + "/";
            String objectName = cleanFolder + UUID.randomUUID().toString() + extension;
            String contentType = file.getContentType();
            if ((contentType == null || contentType.isBlank() || "application/octet-stream".equals(contentType))
                    && ".mp4".equalsIgnoreCase(extension)) {
                contentType = "video/mp4";
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("Successfully uploaded file '{}' to path '{}' in bucket '{}'.", 
                    originalFilename, objectName, bucketName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Error occurred while uploading file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String marker = "/" + bucketName + "/";
            int index = fileUrl.indexOf(marker);
            if (index != -1) {
                String objectName = fileUrl.substring(index + marker.length());
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
                log.info("Successfully deleted object '{}' from bucket '{}'.", objectName, bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileUrl, e);
            throw new RuntimeException("Error occurred while deleting file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return endpoint + "/" + bucketName + "/" + objectName;
    }
}
