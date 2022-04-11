// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.quarkus;

import com.azure.graalvm.samples.quarkus.storage.StorageItem;
import com.azure.graalvm.samples.quarkus.storage.StorageService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/")
public class StorageResource {
	private static final CacheControl NO_CACHE;
	static {
		NO_CACHE = new CacheControl();
		NO_CACHE.setNoCache(true);
	}

	@Inject
	StorageService storageService;

	@Inject
	Template storageExplorer;

	private List<StorageItem> getAllFiles() {
		return storageService.listAllFiles().collect(Collectors.toList());
	}

	@GET()
	public Response listUploadedFiles(@HeaderParam("success") boolean success,
									  @HeaderParam("message") String message) {
		TemplateInstance templateInstance = storageExplorer
			.data("files", getAllFiles());

		if (message != null) {
			templateInstance.data("success", success).data("message", message);
		}

		String body = templateInstance.render();

		return Response.ok(body)
		   .cacheControl(NO_CACHE)
		   .build();
	}

	@GET
	@Path("/files/{filename:.+}")
	public Response serveFile(@PathParam final String filename) {
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

		return Response.ok(storageItem.getContent(), storageItem.getContentType())
		    .cacheControl(NO_CACHE)
		    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + "; filename=\"" + filename + "\"")
			.build();
	}

	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@POST
	@Path("/files/upload")
	public Response handleFileUpload(MultipartFormDataInput input) {
		boolean success = true;

		String filename = "";
		final Map<String, List<InputPart>> formParts = input.getFormDataMap();
		final List<InputPart> inPart = formParts.get("file");
		for (final InputPart inputPart : inPart) {
			try {
				// Retrieve headers, read the Content-Disposition header to obtain the original name of the file
				final MultivaluedMap<String, String> headers = inputPart.getHeaders();
				final String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
				for (final String name : contentDispositionHeader) {
					if ((name.trim().startsWith("filename"))) {
						final String[] tmp = name.split("=");
						filename = tmp[1].trim().replaceAll("\"","");
					}
				}

				// Handle the body of that part with an InputStream
				final byte[] stream = inputPart.getBody(byte[].class,null);

				storageService.store(filename, new ByteArrayInputStream(stream), stream.length);
				/* ..etc.. */
			}
			catch (IOException e) {
				e.printStackTrace();
				success = false;
			}
		}

		final String message = success ?
		   "You successfully uploaded " + filename + "!" :
		   "Failed to upload " + filename;

		return Response.seeOther(UriBuilder.fromPath("/").build())
		    .cacheControl(NO_CACHE)
			.header("success", success)
			.header("message", message)
			.build();
	}

	@GET
	@Path("/files/delete/{filename:.+}")
	public Response deleteFile(@PathParam final String filename) {
		boolean success = storageService.deleteFile(filename);

		String message = success ?
				"You successfully deleted " + filename + "!" :
				"Failed to delete " + filename;

		return Response.seeOther(UriBuilder.fromPath("/").build())
			   .cacheControl(NO_CACHE)
			   .header("success", success)
			   .header("message", message)
			   .build();
	}
}