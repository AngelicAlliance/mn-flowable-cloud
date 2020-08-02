package mn.flowable.cloud.demo;

import com.fasterxml.jackson.databind.JsonNode;
import mn.flowable.cloud.demo.feignclients.ProcessDefinitionClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mn.flowable.cloud.common.model.RemoteUser;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestConfiguration
public class MNFlowbaleDemoTest {

    @Autowired
    ProcessDefinitionClient processDefinitionClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RemoteIdmService remoteIdmService;

    @Test
    public void testFeign() {
        JsonNode s = processDefinitionClient.listProcesDefinitions();
        System.out.println(s);
    }

    @Test
    public void callrestapi() {
        JsonNode resp = restTemplate.getForObject(
                "http://mn-flowable-cloud-restapi/process-api/repository/process-definitions"
                , JsonNode.class);
        System.out.println(resp);

    }

    @Test
    public void testRemoteIdmClient() {
        RemoteUser admin = remoteIdmService.getUser("admin");
        Assertions.assertTrue("admin" == admin.getId());

        remoteIdmService.getUser(null);
    }
}
