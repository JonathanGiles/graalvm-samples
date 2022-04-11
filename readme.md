# Azure SDK for Java GraalVM samples

This directory contains sample applications that demonstrate the use of the [Azure SDK for Java GraalVM support](../azure-graalvm-support) library.

## Getting started

All samples in this directory require environment variables to be set, so that access to appropriate Azure services can be attained. The required environment variables are detailed in the readme documentation for each sample.

Before attempting to compile any of the samples into a native executable, firstly try running it as a Java application. This will provide you with a quicker developer 'inner-loop' as the native image compilation step can take considerable time. All of the samples (unless stated otherwise in their readme documentation) can be executed by calling `mvn exec:java` from within the sample directory.

Once you have managed to run a sample as a Java application, try running it as a native image. Follow these steps:

1) Install GraalVM on your machine by following the [official GraalVM instructions](https://www.graalvm.org/docs/getting-started-with-graalvm/#install-graalvm). 
2) Install the GraalVM `native-image` tool. This tool does not ship with GraalVM, but it can easily be done by running the [GraalVM Updater](https://www.graalvm.org/reference-manual/graalvm-updater/) tool with the following command: `gu install native-image`.
3) Compile the sample of your choice using the command `mvn package -Pnative`.
4) Run the native application by finding the native executable within the `./target` directory within the sample directory.

If all goes well, you should see the equivalent console output as you did when you ran the sample under Java. If issues arise, please [file bug reports](https://github.com/GoogleCloudPlatform/google-cloud-graalvm-support/issues/new).