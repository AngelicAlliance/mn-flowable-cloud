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
import org.mn.flowable.cloud.admin.service.AppDeploymentService;
import org.mn.flowable.cloud.admin.service.exception.FlowableServiceException;
import org.mn.flowable.cloud.admin.util.AbstractClientResource;
import org.mn.flowable.cloud.common.service.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/app")
public class AppDeploymentClientResource extends AbstractClientResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentClientResource.class);

    @Autowired
    protected AppDeploymentService clientService;

    @GetMapping(value = "/rest/admin/app-deployments/{deploymentId}", produces = "application/json")
    public JsonNode getAppDeployment(@PathVariable String deploymentId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig(EndpointType.APP);
        try {
            return clientService.getDeployment(serverConfig, deploymentId);
        } catch (FlowableServiceException e) {
            LOGGER.error("Error getting app deployment {}", deploymentId, e);
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping(value = "/rest/admin/app-deployments/{deploymentId}")
    public void deleteAppDeployment(@PathVariable String deploymentId, HttpServletResponse httpResponse) {
        clientService.deleteDeployment(retrieveServerConfig(EndpointType.APP), httpResponse, deploymentId);
    }
}
