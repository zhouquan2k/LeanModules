package io.leanddd.module.misc;

import io.leanddd.component.data.impl.DictionaryProvider;
import io.leanddd.module.misc.infra.DictionaryMapper;
import io.leanddd.module.misc.model.DictionaryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("Init")
public class ConfigMisc {
    @Bean
    public DictionaryProvider<DictionaryImpl> dictionaryProvider(DictionaryMapper mapper) {
        return new DictionaryProvider<DictionaryImpl>(mapper);
    }
}


