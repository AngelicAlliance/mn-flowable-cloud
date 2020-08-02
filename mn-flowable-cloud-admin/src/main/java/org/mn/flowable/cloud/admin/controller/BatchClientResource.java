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
package org.mn.flowable.cloud.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.mn.flowable.cloud.admin.domain.EndpointType;
import org.mn.flowable.cloud.admin.domain.ServerConfig;
import org.mn.flowable.cloud.admin.service.BatchService;
import org.mn.flowable.cloud.admin.service.exception.FlowableServiceException;
import org.mn.flowable.cloud.admin.util.AbstractClientResource;
import org.mn.flowable.cloud.common.service.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * REST controller for managing the batch.
 */
@RestController
@RequestMapping("/app")
public class BatchClientResource extends AbstractClientResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchClientResource.class);

    @Autowired
    protected BatchService clientService;

    /**
     * GET /rest/admin/batches/{batchId} -> return batch data
     */
    @GetMapping(value = "/rest/admin/batches/{batchId}", produces = "application/json")
    public JsonNode getBatch(@PathVariable String batchId, HttpServletRequest request) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.PROCESS);
        try {
            return clientService.getBatch(serverConfig, batchId);
        } catch (FlowableServiceException e) {
            LOGGER.error("Error getting batch {}", batchId, e);
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * GET /rest/admin/batches/{batchId}/batch-parts
     */
    @GetMapping(value = "/rest/admin/batches/{batchId}/batch-parts", produces = "application/json")
    public JsonNode getBatchParts(@PathVariable String batchId, HttpServletRequest request) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.PROCESS);
        try {
            Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
            return clientService.listBatchParts(serverConfig, batchId, parameterMap);
        } catch (FlowableServiceException e) {
            LOGGER.error("Error getting batch parts for batch {}", batchId, e);
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * DELETE /rest/admin/batches/{batchId} -> delete batch
     */
    @DeleteMapping(value = "/rest/admin/batches/{batchId}", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteJob(@PathVariable String batchId, HttpServletRequest request) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.PROCESS);
        try {
            clientService.deleteBatch(serverConfig, batchId);
        } catch (FlowableServiceException e) {
            LOGGER.error("Error deleting batch {}", batchId, e);
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * GET /rest/admin/batches/{batchId}/batch-document
     */
    @GetMapping(value = "/rest/admin/batches/{batchId}/batch-document", produces = "text/plain")
    public String getBatchDocument(@PathVariable String batchId, HttpServletRequest request) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.PROCESS);
        try {
            return clientService.getBatchDocument(serverConfig, batchId);

        } catch (FlowableServiceException e) {
            LOGGER.error("Error getting batch document {}", batchId, e);
            throw new BadRequestException(e.getMessage());
        }
    }
}
