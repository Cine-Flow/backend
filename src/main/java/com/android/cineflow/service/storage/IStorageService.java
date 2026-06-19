package com.android.cineflow.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    /**
     * Uploads a file to object storage inside a specific folder and returns its accessible URL.
     *
     * @param file   the file to upload
     * @param folder the virtual directory folder in bucket (e.g. "films", "shorts", "football")
     * @return the public URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Deletes a file from object storage by its URL.
     *
     * @param fileUrl the URL of the file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Retrieves or generates the accessible URL for an object name.
     *
     * @param objectName the name of the object
     * @return the URL
     */
    String getFileUrl(String objectName);
}
