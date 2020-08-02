package org.mn.flowable.cloud.admin.feignclients;


import com.fasterxml.jackson.databind.JsonNode;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient("mn-flowable-cloud-restapi")
@RequestMapping("/process-api/repository")
public interface ProcessDefinitionClient {

    @RequestMapping(value = "/process-definitions",
            method = RequestMethod.GET)
    public JsonNode listProcesDefinitions(
            @RequestParam Map<String, String> parameterMap);

    @RequestMapping(value = "/process-definitions/{definitionId}",
            method = RequestMethod.GET)
    public JsonNode getProcessDefinition(
            @PathVariable(value = "definitionId") String definitionId);

    @RequestMapping(value = "/process-definitions/{definitionId}",
            method = RequestMethod.PUT)
    public JsonNode updateProcessDefinitionCategory(
            @PathVariable(value = "definitionId") String definitionId,
            @RequestBody JsonNode updateCall);

    @RequestMapping(value = "/process-definitions/{processDefinitionId}/batch-migrate",
            method = RequestMethod.POST)
    public void migrateInstancesOfProcessDefinition(
            @PathVariable(value = "processDefinitionId") String processDefinitionId,
            @RequestBody String migrationDocument);

    @RequestMapping(value = "/process-definitions/{definitionId}/resourcedata",
            method = RequestMethod.GET)
    public Response getProcessDefinitionModel(
            @PathVariable(value = "definitionId") String definitionId);

}
