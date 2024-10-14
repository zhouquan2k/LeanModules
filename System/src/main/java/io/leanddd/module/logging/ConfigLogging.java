package io.leanddd.module.logging;

import io.leanddd.component.data.RepositoryImpl;
import io.leanddd.component.framework.Repository;
import io.leanddd.component.logging.api.OperateLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.repository.CrudRepository;


// spring data need to have a standalone repository class
interface OperateLogRepository extends CrudRepository<OperateLog, String> {
}

@Configuration
@DependsOn("Init")
public class ConfigLogging {

    @Bean
    public Repository<OperateLog> myOperateLogRepository(OperateLogRepository springRepo) {
        return new RepositoryImpl<OperateLog>(OperateLog.class, springRepo);
    }
}

