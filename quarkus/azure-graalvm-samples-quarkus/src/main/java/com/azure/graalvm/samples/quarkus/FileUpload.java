// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.quarkus;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.File;

public class FileUpload {
    @FormParam("file")
    public File file;

    @FormParam("file")
    @PartType(MediaType.TEXT_PLAIN)
    public String fileName;
}
