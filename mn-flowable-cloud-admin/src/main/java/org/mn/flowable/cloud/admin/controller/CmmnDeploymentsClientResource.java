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
import org.mn.flowable.cloud.admin.service.CmmnDeploymentService;
import org.mn.flowable.cloud.admin.service.exception.FlowableServiceException;
import org.mn.flowable.cloud.admin.util.AbstractClientResource;
import org.mn.flowable.cloud.common.service.exception.BadRequestException;
import org.mn.flowable.cloud.common.service.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/app/rest/admin/cmmn-deployments")
public class CmmnDeploymentsClientResource extends AbstractClientResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmmnDeploymentsClientResource.class);

    @Autowired
    protected CmmnDeploymentService clientService;

    /**
     * GET /rest/admin/cmmn-deployments -> get a list of form deployments.
     */
    @GetMapping(produces = "application/json")
    public JsonNode listCmmnDeployments(HttpServletRequest request) {
        LOGGER.debug("REST request to get a list of form deployments");

        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.CMMN);
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

        try {
            resultNode = clientService.listDeployments(serverConfig, parameterMap);

        } catch (FlowableServiceException e) {
            LOGGER.error("Error getting form deployments", e);
            throw new BadRequestException(e.getMessage());
        }

        return resultNode;
    }

    /**
     * POST /rest/admin/cmmn-deployments: upload a form deployment
     */
    @PostMapping(produces = "application/json")
    public JsonNode handleCmmnFileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                ServerConfig serverConfig = retrieveServerConfig(EndpointType.CMMN);
                String fileName = file.getOriginalFilename();
                if (fileName != null && (fileName.endsWith(".cmmn") || fileName.endsWith(".xml"))) {

                    return clientService.uploadDeployment(serverConfig, fileName, file.getInputStream());

                } else {
                    LOGGER.error("Invalid cmmn deployment file name {}", fileName);
                    throw new BadRequestException("Invalid file name");
                }

            } catch (IOException e) {
                LOGGER.error("Error deploying form upload", e);
                throw new InternalServerErrorException("Could not deploy file: " + e.getMessage());
            }

        } else {
            LOGGER.error("No cmmn deployment file found in request");
            throw new BadRequestException("No file found in POST body");
        }
    }

}
