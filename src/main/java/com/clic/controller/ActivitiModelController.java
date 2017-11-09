package com.clic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.StreamResource;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_ID;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

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
    @Autowired
    private ObjectMapper objectMapper;

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
     * @author moafmoar
     * @date 2017-11-03
     *
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String  create(@RequestParam("name") String name, @RequestParam("key") String key,
                       @RequestParam(value = "description", required = false) String description, HttpServletRequest request,
                       HttpServletResponse response) {
        String openUrl = null;
        String modelId = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(MODEL_DESCRIPTION,
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
            //repositoryService.getBpmnModel(newModel.getId());
            modelId = newModel.getId();
            System.out.println(newModel.getId() + "===========newModel.getId()==============");
            System.out.println("url:"+request.getContextPath() + "/modeler.html?modelId=" + newModel.getId());
            //response.sendRedirect("http://10.68.68.116:8090/ActivitiPro/modeler.html?modelId=" + newModel.getId());
            openUrl = "http://10.68.68.116:8090/ActivitiPro/modeler.html?modelId=" + newModel.getId();
           /* ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(newModel.getId()));
            byte[] bpmnBytes = null;
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            String processName = newModel.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(newModel.getName()).addString(processName, new String(bpmnBytes,"utf-8")).deploy();*/


            /*Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode1 = new ObjectMapper()
                    .readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode1);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            // IOUtils.copy(in, response.getOutputStream());
            OutputStream os = response.getOutputStream();
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);*/
        } catch (Exception e) {
            e.getStackTrace();
        }
        return modelId;
    }


    /**
     * 模型列表
     * @author moafmoar
     * @date 2017-11-03
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public Object list() {
        List<Model> list = repositoryService.createModelQuery().list();
        List<Model> resultList =  repositoryService.createModelQuery().listPage(0, 10); //分页查询

        return list;
    }

    /**
     * 获取流程图
     *@author moafmoar
     * @date 2017-11-03
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
     * @author moafmoar
     * @date 2017-11-03
     *
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public String delete(String modelId) {
        if (null != modelId){
            repositoryService.deleteModel(modelId);
            return "OK";
        }
        return "false";

    }

    /**
     *暂时未用
     * 编辑模型前,读取模型
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put(MODEL_NAME, model.getName());
                }
                modelNode.put(MODEL_ID, model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper
                        .readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.put("model", editorJsonNode);

            } catch (Exception e) {
                LOGGER.error("Error creating model JSON", e);
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;
    }


    /**
     * 保存模型
     * 暂时未用
     */
    @RequestMapping(value = "/{modelId}/save", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        try {

            Model model = repositoryService.getModel(modelId);

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

            modelJson.put(MODEL_NAME, values.getFirst("name"));
            modelJson.put(MODEL_DESCRIPTION, values.getFirst("description"));
            model.setMetaInfo(modelJson.toString());
            model.setName(values.getFirst("name"));
            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), values.getFirst("json_xml").getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(values.getFirst("svg_xml").getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();

        } catch (Exception e) {
            LOGGER.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }

    /**
     * @desc 根据流程Id获取流程信息
     * @author moafmoar
     * @date 2017-11-08
     * @param modelid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getModelById")
    public Map<String,Object> getModelById(String modelid){
        Model model = repositoryService.getModel(modelid);
        Map<String,Object> modelMap  = new HashMap<>();
        if (null!= model){
            //JSONObject jsonObject = new JSONObject();

            modelMap.put("id",model.getId());
            modelMap.put("name",model.getName());
            modelMap.put("createTime",model.getCreateTime());
            modelMap.put("version",model.getVersion());
        }

        return modelMap;
    }


    /**
     * @desc 流程部署，并返回部署Id
     * @author moafmoar
     * @date 2017-11-09
     * @param modelId
     * @return
     */
    @RequestMapping("/deploy")
    @ResponseBody
    public Object deploy(String modelId) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper()
                    .readTree(repositoryService.getModelEditorSource(modelData.getId()));
            byte[] bpmnBytes = null;
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            String processName = modelData.getName() + ".bpmn20.xml";
            /*Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "utf-8"))
                    .addClasspathResource("public/form/start.form")
                    .addClasspathResource("public/form/leader.form")
                    .deploy();*/
            Deployment deployment1 = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes,"utf-8")).deploy();
            return deployment1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
