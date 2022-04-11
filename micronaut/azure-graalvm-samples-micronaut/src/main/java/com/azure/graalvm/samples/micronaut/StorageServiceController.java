// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.micronaut;

import com.azure.graalvm.samples.micronaut.storage.StorageItem;
import com.azure.graalvm.samples.micronaut.storage.StorageService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.views.View;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class StorageServiceController {

	@Inject
	private StorageService storageService;

	private List<StorageItem> getAllFiles() {
		return storageService.listAllFiles().collect(Collectors.toList());
	}

	@Get("/")
	@View("uploadForm")
	public HttpResponse listUploadedFiles() {
		return HttpResponse
			   .ok(CollectionUtils.mapOf("files", getAllFiles()))
			   .header("Cache-Control", "no-cache");
	}

	@Get("/files/{filename:.+}")
	public HttpResponse<InputStream> serveFile(@PathVariable final String filename) {
		final StorageItem storageItem = storageService.getFile(filename);

		String contentDisposition;
		switch (storageItem.getContentDisplayMode()) {
			default:
			case DOWNLOAD: {
				contentDisposition = "attachment";
				break;
			}
			case MODAL_POPUP:
			case NEW_BROWSER_TAB: {
				contentDisposition = "inline";
			}
		}

		return HttpResponse.ok(storageItem.getContent())
		   .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + "; filename=\"" + filename + "\"")
		   .contentType(MediaType.of(storageItem.getContentType()));
	}

	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Post("/")
	@View("uploadForm")
	public HttpResponse<?> handleFileUpload(final CompletedFileUpload file) {
		boolean success = false;
		try {
			storageService.store(file.getFilename(), file.getInputStream(), file.getSize());
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return HttpResponse.redirect(HttpResponse.uri("/"))
		    .body(CollectionUtils.mapOf("files", getAllFiles()))
		    .setAttribute("success", success)
			.setAttribute("message", success ?
					 "You successfully uploaded " + file.getFilename() + "!" :
					 "Failed to upload " + file.getFilename());
	}

	@Get("/files/delete/{filename:.+}")
	@View("uploadForm")
	public HttpResponse<?> deleteFile(@PathVariable final String filename) {
		boolean success = storageService.deleteFile(filename);

		return HttpResponse.redirect(HttpResponse.uri("/"))
		    .body(CollectionUtils.mapOf("files", getAllFiles()))
		    .setAttribute("success", success)
		    .setAttribute("message", success ?
					"You successfully deleted " + filename + "!" :
					"Failed to deelete " + filename);
	}
}