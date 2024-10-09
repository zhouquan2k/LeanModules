package io.leanddd.module.logging.model;

import io.leanddd.component.logging.api.OperateLog;
import org.springframework.data.repository.CrudRepository;


public interface OperateLogRepository extends CrudRepository<OperateLog, String> {

}
