package io.leanddd.module.security.api;

import io.leanddd.component.meta.Metadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@RequestMapping("/api/public/metadata")
public interface MetadataService {

    @GetMapping
    Metadata getMetadata(@RequestParam(name = "lang", required = false, defaultValue = "en") Locale locale);
}
