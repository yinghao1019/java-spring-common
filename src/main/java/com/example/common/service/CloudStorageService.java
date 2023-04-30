package com.example.common.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CloudStorageService {

    public List<String> listObjectName(String bucketName, String directoryPrefix) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(directoryPrefix),
            Storage.BlobListOption.currentDirectory());

        List<String> blobNameList = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
            if (!blob.isDirectory()) {
                String fullName = blob.getName();
                blobNameList.add(fullName.substring(directoryPrefix.length()));
            }
        }
        return blobNameList;
    }

    public Page<Blob> listObject(String bucketName, String directoryPrefix) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(directoryPrefix),
            Storage.BlobListOption.currentDirectory());
        return blobs;
    }

    public boolean blobIsExist(String bucketName, String objectName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(bucketName, objectName);
        if (blob != null && blob.exists()) {
            return true;
        }
        return false;
    }

    public byte[] downloadObject(String bucketName, String objectName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        return storage.readAllBytes(bucketName, objectName);
    }

    public Blob saveFile(String bucketName, String des, byte[] file) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, des);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        return storage.create(blobInfo, file);
    }

    public void copyBlobInSameBucket(String bucketName, String srcBlobId, String desBlobId) throws StorageException {
        BlobId sourceBlobId = BlobId.of(bucketName, srcBlobId);
        BlobId targetBlobId = BlobId.of(bucketName, desBlobId);
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Storage.CopyRequest request = Storage.CopyRequest.newBuilder().setSource(sourceBlobId).setTarget(targetBlobId)
            .build();
        storage.copy(request);
    }

    public boolean deleteObject(String bucketName, String des) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, des);
        return storage.delete(blobId);
    }
}
