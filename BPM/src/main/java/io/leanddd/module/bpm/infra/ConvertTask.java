package io.leanddd.module.bpm.infra;

import io.leanddd.module.bpm.api.BpmTask;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ConvertTask {

    @Mapping(target = "pendingTask", ignore = true)
    @Mapping(target = "businessKey", ignore = true)
    @Mapping(target = "nodeDefId", source = "taskDefinitionKey")
    BpmTask taskToBpmTask(TaskInfo src);

    List<BpmTask> tasksToBpmTasks(List<Task> source);

    List<BpmTask> historicTasksToBpmTasks(List<HistoricTaskInstance> source);
}
