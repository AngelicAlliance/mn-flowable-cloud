package mn.flowable.cloud.demo.feignclients;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient("mn-flowable-cloud-restapi")
@RequestMapping("/process-api/repository")
public interface ProcessDefinitionClient {

    @RequestMapping(value = "/process-definitions", method = RequestMethod.GET)
    public JsonNode listProcesDefinitions();

}

