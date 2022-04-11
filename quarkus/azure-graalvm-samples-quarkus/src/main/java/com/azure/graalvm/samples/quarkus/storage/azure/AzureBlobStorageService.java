// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.quarkus.storage.azure;

import com.azure.graalvm.samples.quarkus.storage.StorageItem;
import com.azure.graalvm.samples.quarkus.storage.StorageService;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.stream.Stream;

/**
 * Azure Blob Storage implementation of the StorageService interface, allowing for access to Azure Blob Storage to
 * store, retrieve, and delete files.
 */
@Singleton
public class AzureBlobStorageService implements StorageService {
    @ConfigProperty(name = "azure.storage.blob.container-name")
    String blobStorageContainerName;

    @ConfigProperty(name = "azure.storage.blob.connection-string")
    String blobStorageConnectionString;

    private BlobContainerClient blobContainerClient;

    @Override
    @PostConstruct
    public void init() {
        if (blobContainerClient != null) {
            return;
        }

        boolean doInit = true;

        if (blobStorageContainerName == null || blobStorageContainerName.isEmpty()) {
            System.err.println("Error: Please set the azure.storage.blob.container property as outlined in this samples readme file");
            doInit = false;
        }

        if (blobStorageConnectionString == null || blobStorageConnectionString.isEmpty()) {
            System.err.println("Error: Please set the azure.storage.blob.connection-string property as outlined in this samples readme file");
            doInit = false;
        }

        if (!doInit) {
            System.exit(-1);
            return;
        }

        final BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
              .connectionString(blobStorageConnectionString)
              .buildClient();

        blobContainerClient = blobServiceClient.getBlobContainerClient(blobStorageContainerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient = blobServiceClient.createBlobContainer(blobStorageContainerName);
        }
    }

    @Override
    public void store(final String filename, final InputStream inputStream, final long length) {
        final BlobClient blobClient = blobContainerClient.getBlobClient(filename);
        blobClient.upload(inputStream, length);

        final String mimeType = URLConnection.guessContentTypeFromName(filename);
        blobClient.setHttpHeaders(new BlobHttpHeaders()
            .setContentType(mimeType));
    }

    @Override
    public Stream<StorageItem> listAllFiles() {
        return blobContainerClient.listBlobs().stream().map(AzureStorageItem::new);
    }

    @Override
    public StorageItem getFile(String filename) {
        return new AzureStorageItem(blobContainerClient.getBlobClient(filename));
    }

    @Override
    public boolean deleteFile(String filename) {
        final BlobClient blobClient = blobContainerClient.getBlobClient(filename);
        if (blobClient.exists()) {
            blobClient.delete();
            return true;
        }
        return false;
    }
}
