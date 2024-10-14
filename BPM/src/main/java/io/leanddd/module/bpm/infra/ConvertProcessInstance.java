package io.leanddd.module.bpm.infra;

import io.leanddd.module.bpm.api.BpmProcessInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ConvertProcessInstance {
    @Mapping(target = "instanceId", source = "processInstanceId")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "status", ignore = true)
    BpmProcessInstance instanceToBpmInstance(ProcessInstance instance);

    List<BpmProcessInstance> instancesToBpmInstances(List<HistoricProcessInstance> instances);

    @Mapping(target = "instanceId", source = "id")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "status", ignore = true)
    BpmProcessInstance instanceToBpmInstance(HistoricProcessInstance instance);
}

