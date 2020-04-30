package com.activiti.service;

import com.activiti.mapper.WorkflowUserTaskMapper;
import com.activiti.pojo.WorkflowUserTask;
import org.activiti.engine.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowUserTaskService {
	
	@Resource
	WorkflowUserTaskMapper workflowUserTaskMapper;
	@Autowired
	private HistoryService historyService;
	
	public List<WorkflowUserTask> listWorkflowUserTaskByDefKeyAndTaskKey(String procDefcKey, String taskKey){
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("procDefKey",procDefcKey);
		params.put("taskDefKey",taskKey);

		return workflowUserTaskMapper.listWorkflowUserTaskByDefKeyAndTaskKey(params);
	}


	public WorkflowUserTask getOneByWorkflowUserTaskByDefKeyAndTaskKey(String procDefcKey, String taskKey){
		return workflowUserTaskMapper.getOneByWorkflowUserTaskByDefKeyAndTaskKey(procDefcKey,taskKey);
	}


	/**
	 * 获取流程图 审批时
	 * @param params
	 * @return
	 */
	public Map<String, Object> getBpmnData(Map<String, Object> params){

		String instanceId = params.get("instanceId").toString();

		Map<String, Object> map = new HashMap<>();
		if (!StringUtils.isEmpty(instanceId)) {
			map.put("content", workflowUserTaskMapper.getBpmnData(instanceId));
			map.put("finish", historyService.createHistoricActivityInstanceQuery().processInstanceId(instanceId).finished().list());
			return map;
		}
		return null;
	}
}
