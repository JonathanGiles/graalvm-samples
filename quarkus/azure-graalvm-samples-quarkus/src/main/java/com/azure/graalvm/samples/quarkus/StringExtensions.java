// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.quarkus;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension(namespace = "str")
class StringExtensions {

   static String format(String fmt, Object... args) {
      return String.format(fmt, args);
   }
}