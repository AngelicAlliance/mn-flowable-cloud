/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mn.flowable.cloud.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import feign.Response;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.mn.flowable.cloud.admin.domain.ServerConfig;
import org.mn.flowable.cloud.admin.feignclients.ProcessDefinitionClient;
import org.mn.flowable.cloud.admin.service.exception.FlowableServiceException;
import org.mn.flowable.cloud.admin.util.FlowableClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for invoking Flowable REST services.
 */
@Service
public class ProcessDefinitionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionService.class);

    @Autowired
    protected FlowableClientService clientUtil;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    ProcessDefinitionClient processDefinitionClient;

    public JsonNode listProcesDefinitions(ServerConfig serverConfig,
                                          Map<String, String[]> parameterMap, boolean latest) {

        /*URIBuilder builder = null;
        try {
            builder = new URIBuilder("repository/process-definitions");
        } catch (Exception e) {
            LOGGER.error("Error building uri", e);
            throw new FlowableServiceException("Error building uri", e);
        }

        for (String name : parameterMap.keySet()) {
            builder.addParameter(name, parameterMap.get(name)[0]);
        }
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
        return clientUtil.executeRequest(get, serverConfig);
*/
        Map<String, String> map = new HashMap<>();
        for (String name : parameterMap.keySet()) {
            map.put(name, parameterMap.get(name)[0]);
        }

        return processDefinitionClient.listProcesDefinitions(map);
    }

    public JsonNode getProcessDefinition(ServerConfig serverConfig, String definitionId) {
        /*HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "repository/process-definitions/" + definitionId));
        return clientUtil.executeRequest(get, serverConfig);*/
        return processDefinitionClient.getProcessDefinition(definitionId);
    }

    public JsonNode updateProcessDefinitionCategory(ServerConfig serverConfig, String definitionId, String category) {
        ObjectNode updateCall = objectMapper.createObjectNode();
        updateCall.put("category", category);

        /*URIBuilder builder = clientUtil.createUriBuilder("repository/process-definitions/" + definitionId);

        HttpPut put = clientUtil.createPut(builder, serverConfig);
        put.setEntity(clientUtil.createStringEntity(updateCall));

        return clientUtil.executeRequest(put, serverConfig);*/
        return processDefinitionClient.updateProcessDefinitionCategory(definitionId, updateCall);
    }

    public BpmnModel getProcessDefinitionModel(ServerConfig serverConfig, String definitionId) {
        //HttpGet request = new HttpGet(clientUtil.getServerUrl(serverConfig, "repository/process-definitions/" + definitionId + "/resourcedata"));
        //return executeRequestForXML(get, serverConfig, HttpStatus.SC_OK);

        FlowableServiceException exception = null;
        //CloseableHttpClient client = clientUtil.getHttpClient(serverConfig);
        try {
            Response response = processDefinitionClient.getProcessDefinitionModel(definitionId);
            //CloseableHttpResponse response = client.execute(request);
            InputStream responseContent = response.body().asInputStream();
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(responseContent, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            boolean success = response.status() == HttpStatus.SC_OK;

            if (success) {
                return bpmnModel;
            } else {
                exception = new FlowableServiceException("An error occurred while calling Flowable: " + response.status());
            }
        } catch (Exception e) {
            LOGGER.error("Error getProcessDefinitionModel {}", definitionId, e);
            if (e instanceof HttpHostConnectException) {
                throw new FlowableServiceException("Unable to connect to the Flowable server.");
            } else if (e instanceof ConnectTimeoutException) {
                throw new FlowableServiceException("Connection to the Flowable server timed out.");
            } else {
                throw new FlowableServiceException(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        if (exception != null) {
            throw exception;
        }

        return null;
    }

    public void migrateInstancesOfProcessDefinition(ServerConfig serverConfig, String processDefinitionId, String migrationDocument) throws FlowableServiceException {
        /*try {
            URIBuilder builder = clientUtil.createUriBuilder("repository/process-definitions/" + processDefinitionId + "/batch-migrate");
            HttpPost post = clientUtil.createPost(builder.build().toString(), serverConfig);

            post.setEntity(clientUtil.createStringEntity(migrationDocument));
            clientUtil.executeRequestNoResponseBody(post, serverConfig, HttpStatus.SC_OK);

        } catch (Exception e) {
            throw new FlowableServiceException(e.getMessage(), e);
        }*/
        processDefinitionClient.migrateInstancesOfProcessDefinition(processDefinitionId, migrationDocument);
    }

}
