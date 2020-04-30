package com.activiti.service;

import com.activiti.pojo.*;
import com.activiti.utils.ResultUtil;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ActivitiRunService {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private WorkflowAuditService workflowAuditService;


    /**
     * 开启流程
     * @param activitiApply
     * @return
     */
    public Result startProcess(ActivitiApply activitiApply){

        if(StringUtils.isBlank(activitiApply.getKey())){
            return ResultUtil.operatDataResult(0,null,"流程定义不能为空！");
        }else if(StringUtils.isBlank(activitiApply.getApplyUserId())){
            return ResultUtil.operatDataResult(0,null,"发起人不能为空！");
        }else{

            String key = activitiApply.getKey();

            //查询流程是否部署
            ProcessDefinition processDefinition= repositoryService
                    .createProcessDefinitionQuery().processDefinitionKey(key)
                    .latestVersion().singleResult();

            if(processDefinition == null){
                return ResultUtil.operatDataResult(0,key,"流程未部署！");
            }else{
                //获取受理人节点
                Map<String, Object> variables = new HashMap<String, Object>();
                String applyUserId = activitiApply.getApplyUserId();
                //设置申请人
                variables.put("applyUserId",applyUserId);
                variables.put("flowType", "normal");
                //设置发请人
                identityService.setAuthenticatedUserId(applyUserId);
                //启动流程
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
                //返回流程id
                String processId = processInstance.getProcessInstanceId();
                return ResultUtil.operatDataResult(1,processId,"流程启动成功！");
            }
        }
    }



    /**
     * 获取审批列表
     * @param map
     * @return
     */
    public List<ActivitiProcess> getProcessList(Map<String,Object> map){

        List<ActivitiProcess> activitiProcesses = new ArrayList<ActivitiProcess>();

        String roleCode = "";

        if(!map.containsKey("roleCode")){
            return activitiProcesses;
        }else{
            roleCode = map.get("roleCode").toString();
        }


        List<Task> taskList = taskService.createTaskQuery()
                .taskAssignee(roleCode).orderByTaskCreateTime().desc()
                .list();

        for(Task task : taskList){
            ActivitiProcess activitiProcess = new ActivitiProcess();
            String processInstanceId = task.getProcessInstanceId();
            //获取历史节点
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).orderByProcessInstanceStartTime().asc().singleResult();

            activitiProcess.setProcessInstanceId(processInstanceId);
            activitiProcess.setProcessDefinitionId(task.getProcessDefinitionId());
            activitiProcess.setProcessName(hpi.getProcessDefinitionName());
            activitiProcess.setStaffId(hpi.getStartUserId());
            activitiProcesses.add(activitiProcess);
        }

        return activitiProcesses;
    }




    @Transactional(propagation = Propagation.REQUIRED)
    public Object approvelProcess(WorkflowAudit workflowAudit) {
        //审批状态
        Integer approved = workflowAudit.getStatus();
        //获取任务instanceId
        String instanceId = workflowAudit.getProcessInstanceId();

        Task task = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        String taskId = "";
        if(task != null){
            taskId = task.getId();
        }else{
            return ResultUtil.operatDataResult(0,instanceId,"流程不存在！");
        }

        Map<String, Object> variables = new HashMap<>();

        //跳转条件
        if(approved == 1){
            variables.put("approved", "true");
        }else{
            variables.put("approved", "false");
        }

        taskService.complete(taskId, variables);

        //插入记录 WorkflowAudit
        workflowAudit.setTaskId(taskId);
        workflowAudit.setTaskName(task.getName());
        workflowAudit.setAuditRoleCode(task.getAssignee());
        //查询历史获取审批人
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).orderByProcessInstanceStartTime().asc().singleResult();
        workflowAudit.setApplyStaffId(hpi.getStartUserId());
        workflowAuditService.add(workflowAudit);

        return ResultUtil.operatDataResult(1 , workflowAudit , "审批成功!");
    }

}
