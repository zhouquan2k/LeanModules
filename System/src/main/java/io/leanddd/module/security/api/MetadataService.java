package io.leanddd.module.security.api;

import io.leanddd.component.meta.Metadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/public/metadata")
public interface MetadataService {

    @GetMapping
    Metadata getMetadata();
}
