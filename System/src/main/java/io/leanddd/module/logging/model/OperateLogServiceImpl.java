package io.leanddd.module.logging.model;

import io.leanddd.component.framework.Repository;
import io.leanddd.component.logging.api.OperateLog;
import io.leanddd.component.logging.api.OperateLogService;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Command.LogType;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.logging.infra.OperateLogMapper;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@Service(type = Type.Mixed, name = "log", order = 104)
@RequiredArgsConstructor
public class OperateLogServiceImpl implements OperateLogService {

    private final Repository<OperateLog> repository;
    private final OperateLogMapper mapper;

    @Override
    @Command(log = LogType.No)
    public void persist(OperateLog log) {
        repository.save(log);
    }

    @Override
    public List<OperateLog> queryOperateLogs(Map<String, Object> example) {
        return mapper.queryByExample(example);
    }

    @Override
    public List<OperateLog> queryOperateLogs() {
        return mapper.queryByExample(Map.of());
    }

}
