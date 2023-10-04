package com.dedalus.xraycucumber.ui.utils;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;

public class FileServiceParametersLoader {

    private final ObjectMapper objectMapper;

    public FileServiceParametersLoader() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
    }

    public ServiceParameters load(VirtualFile serviceParametersFile) throws IOException {
        try (InputStream inputStream = serviceParametersFile.getInputStream()) {
            return objectMapper.readValue(inputStream, ServiceParameters.class);
        } catch(IOException e) {
            throw new IOException(e);
        }
    }
}
