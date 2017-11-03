package com.clic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.StreamResource;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by DBQ on 2017/1/16.
 */
//@Component
@Controller
@RequestMapping("/model")
public class ActivitiModelController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ActivitiModelController.class);

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    FormService formService;
    @Autowired
    private RepositoryService repositoryService;

   /* @Autowired
    private ObjectMapper objectMapper;*/

    /*public void test(){
        runtimeService.createProcessInstanceQuery().processDefinitionId("").list();
        historyService.createHistoricTaskInstanceQuery().taskAssignee("").finished().list();


        String parentTaskId = taskService.createTaskQuery().singleResult().getParentTaskId();
        formService.getTaskFormData(parentTaskId).getFormProperties();

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionId("").singleResult();


        taskService.createTaskQuery().singleResult().getCreateTime();


        historyService.createHistoricProcessInstanceQuery().unfinished().singleResult().getDurationInMillis();


    }*/
    /**
     * 创建模型
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public void create(@RequestParam("name") String name, @RequestParam("key") String key,
                       @RequestParam(value = "description", required = false) String description, HttpServletRequest request,
                       HttpServletResponse response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION,
            org.apache.commons.lang3.StringUtils.defaultString(description));
            Model newModel = repositoryService.newModel();
            newModel.setMetaInfo(modelObjectNode.toString());
            newModel.setName(name);
            newModel.setKey(key);

            repositoryService.saveModel(newModel);
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(newModel.getId(), editorNode.toString().getBytes("utf-8"));
            System.out.println(newModel.getId() + "===========newModel.getId()==============");
            System.out.println("url:"+request.getContextPath() + "/modeler.html?modelId=" + newModel.getId());
            response.sendRedirect(request.getContextPath()+"/modeler.html?modelId=" + newModel.getId());
        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    /**
     * 模型列表
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public Object list() {
        List<Model> list = repositoryService.createModelQuery().list();

        return list;
    }

    /**
     * 获取流程图
     *
     * @throws IOException
     */
    @RequestMapping("/getImage")
    public void getImage(String modelId, HttpServletResponse response) throws IOException {
        StreamResource.StreamSource streamSource = null;
        final byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelId);
        // final byte[] editorSource =
        // repositoryService.getModelEditorSource(modelId);
        //ProcessDefinition processDefinition = repositoryService.getProcessDefinition(modelId);
        //String startFormData = formService.getStartFormData(modelId).toString();

       // List<FormProperty> formProperties = startFormData.getFormProperties();
        if (editorSourceExtra != null) {
            streamSource = new StreamResource.StreamSource() {
                private static final long serialVersionUID = 1L;

                public InputStream getStream() {
                    InputStream inStream = null;
                    try {
                        inStream = new ByteArrayInputStream(editorSourceExtra);
                    } catch (Exception e) {
                        LOGGER.warn("Error reading PNG in StreamSource", e);
                    }
                    return inStream;
                }
            };
        }
        if (streamSource != null) {
            response.setContentType("application/x-msdownload;charset=UTF-8");
            response.reset();// 清除缓冲中的数据
            int temp;
            OutputStream os = null;
            InputStream is = streamSource.getStream();
            try {
                os = response.getOutputStream();
                while ((temp = is.read()) != (-1)) {
                    os.write(temp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 删除模型
     */
    @RequestMapping(value = "/delete")
    public String delete(String modelId) {
        repositoryService.deleteModel(modelId);
        return "OK";
    }
}
