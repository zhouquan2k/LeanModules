package io.leanddd.module.bpm.model;

import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.meta.Service;
import io.leanddd.module.bpm.api.BpmProcessInstance;
import io.leanddd.module.bpm.api.BpmProcessInstanceCreateReq;
import io.leanddd.module.bpm.api.BpmService;
import io.leanddd.module.bpm.api.BpmTask;
import io.leanddd.module.bpm.infra.ConvertProcessInstance;
import io.leanddd.module.bpm.infra.ConvertTask;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@RequiredArgsConstructor
@Service
public class BpmServiceImpl implements BpmService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final ConvertTask convertTask;
    private final ConvertProcessInstance convertInstance;

    private Optional<ProcessDefinition> getProcessDefinition(String key) {
        return Optional.ofNullable(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult());
    }

    @Override
    public String createProcessInstance(BpmProcessInstanceCreateReq req) {

        ProcessDefinition definition = getProcessDefinition(req.getProcessDefinitionKey()).orElseThrow();
        Util.check(!definition.isSuspended());

        Authentication.setAuthenticatedUserId(Context.getUserId());
        ProcessInstance instance = runtimeService.startProcessInstanceById(definition.getId(), req.getBusinessKey(),
                req.getVariables());

        // set a meaningful name from request
        runtimeService.setProcessInstanceName(instance.getId(), req.getInstanceName());

        return instance.getId();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        var task = getBpmTask(taskId);
        if (Util.isEmpty(task.getAssignee())) {
            taskService.claim(taskId, Context.getUserId());
        }
        taskService.complete(taskId, variables);
    }

    @Override
    public List<BpmTask> queryMyTasks() {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(Context.getUserId())
                .includeProcessVariables().orderByTaskCreateTime()
                .asc().list();
        return convertTask.tasksToBpmTasks(tasks);
    }

    @Override
    public List<BpmProcessInstance> queryMyHistoryInstances() {
        var userId = Context.getUserId();
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId);
        List<HistoricTaskInstance> taskInstances = taskQuery.list();
        Set<String> processInstanceIds = taskInstances.stream()
                .map(HistoricTaskInstance::getProcessInstanceId)
                .collect(Collectors.toSet());

        if (!processInstanceIds.isEmpty()) {
            var instances = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceIds(processInstanceIds)
                    .orderByProcessInstanceStartTime().desc()
                    .listPage(0, 10);
            return convertInstance.instancesToBpmInstances(instances);
        }
        return List.of();
    }

    @Override
    public void claimTask(String taskId, String userId) {
        this.taskService.claim(taskId, userId);
    }

    private BpmTask getBpmTask(String taskId) {
        TaskInfo task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmTask bpmTask = null;
        if (task == null) {
            task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            Util.check(task != null);
            bpmTask = convertTask.taskToBpmTask(task);
            bpmTask.setPendingTask(false);
        } else {
            bpmTask = convertTask.taskToBpmTask(task);
            bpmTask.setPendingTask(true);
            if (bpmTask.getProcessVariables().isEmpty()) {
                bpmTask.setProcessVariables(runtimeService.getVariables(task.getProcessInstanceId()));
            }
        }
        return bpmTask;
    }

    @Override
    public BpmTask getTask(String taskId) {
        BpmTask task = getBpmTask(taskId);
        // TODO security checks
        return task;
    }

    @Override
    public List<BpmTask> getCurrentTasks(String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        return convertTask.tasksToBpmTasks(tasks);
    }

    @Override
    public BpmProcessInstance getInstanceByBusinessKey(String businessKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey).singleResult();
        if (processInstance == null)
            return null;
        var tasks = getCurrentTasks(processInstance.getProcessInstanceId());
        var bpmInstance = convertInstance.instanceToBpmInstance(processInstance);
        bpmInstance.setTasks(tasks);
        return bpmInstance;
    }
}
