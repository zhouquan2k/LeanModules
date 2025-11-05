package io.leanddd.module.security.api;

import io.leanddd.component.meta.Metadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Map;

@RequestMapping("/api/public")
public interface MetadataService {

    @GetMapping("/metadata")
    Metadata getMetadata(@RequestParam(name = "lang", required = false, defaultValue = "en") Locale locale);


    @GetMapping("/env")
    Map<String, String> getEnvironmentVariables();
}
