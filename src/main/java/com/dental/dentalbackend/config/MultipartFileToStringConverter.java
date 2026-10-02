package com.dental.dentalbackend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles graceful conversion when a form parameter matching a String property is sent as a MultipartFile.
 */
@Component
public class MultipartFileToStringConverter implements Converter<MultipartFile, String> {

    @Override
    public String convert(MultipartFile source) {
        return source.isEmpty() ? null : source.getOriginalFilename();
    }
}
