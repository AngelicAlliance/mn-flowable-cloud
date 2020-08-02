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
package org.mn.flowable.cloud.modeler.service;

import org.apache.http.HttpStatus;
import org.flowable.idm.api.User;
import org.mn.flowable.cloud.common.properties.FlowableCommonAppProperties;
import org.mn.flowable.cloud.common.service.exception.InternalServerErrorException;
import org.mn.flowable.cloud.common.util.TenantProvider;
import org.mn.flowable.cloud.modeler.domain.AppDefinition;
import org.mn.flowable.cloud.modeler.domain.Model;
import org.mn.flowable.cloud.modeler.properties.FlowableModelerAppProperties;
import org.mn.flowable.cloud.modeler.serviceapi.AppDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Can't merge this with {@link AppDefinitionService}, as it doesn't have visibility of domain models needed to do the publication.
 *
 * @author jbarrez
 */
@Service
@Transactional
public class AppDefinitionPublishService extends BaseAppDefinitionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDefinitionPublishService.class);

    protected final FlowableCommonAppProperties properties;
    protected final FlowableModelerAppProperties modelerAppProperties;
    @Autowired
    protected TenantProvider tenantProvider;
    @Autowired
    RestTemplate restTemplate;

    public AppDefinitionPublishService(FlowableCommonAppProperties properties, FlowableModelerAppProperties modelerAppProperties) {
        this.properties = properties;
        this.modelerAppProperties = modelerAppProperties;
    }

    public void publishAppDefinition(String comment, Model appDefinitionModel, User user) {

        // Create new version of the app model
        modelService.createNewModelVersion(appDefinitionModel, comment, user);

        String deployableZipName = appDefinitionModel.getKey() + ".zip";

        AppDefinition appDefinition = null;
        try {
            appDefinition = resolveAppDefinition(appDefinitionModel);
        } catch (Exception e) {
            LOGGER.error("Error deserializing app {}", appDefinitionModel.getId(), e);
            throw new InternalServerErrorException("Could not deserialize app definition");
        }

        if (appDefinition != null) {
            byte[] deployZipArtifact = createDeployableZipArtifact(appDefinitionModel, appDefinition);

            if (deployZipArtifact != null) {
                deployZipArtifact(deployableZipName, deployZipArtifact, appDefinitionModel.getKey(), appDefinitionModel.getName());
            }
        }
    }

    protected void deployZipArtifact(String artifactName, byte[] zipArtifact, String deploymentKey, String deploymentName) {
        String deployApiUrl = modelerAppProperties.getDeploymentApiUrl();
        Assert.hasText(deployApiUrl, "flowable.modeler.app.deployment-api-url must be set");

        String tenantId = tenantProvider.getTenantId();
        if (!deployApiUrl.endsWith("/")) {
            deployApiUrl = deployApiUrl.concat("/");
        }
        deployApiUrl = deployApiUrl.concat(String.format("app-repository/deployments?deploymentKey=%s&deploymentName=%s",
                encode(deploymentKey), encode(deploymentName)));

        if (tenantId != null) {
            StringBuilder sb = new StringBuilder(deployApiUrl);
            sb.append("&tenantId=").append(encode(tenantId));
            deployApiUrl = sb.toString();
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("artifact", new ByteArrayResource(zipArtifact))
                .filename(artifactName);
        ResponseEntity<Object> objectResponseEntity = restTemplate.postForEntity(deployApiUrl, builder.build(), Object.class);
        if (objectResponseEntity.getStatusCodeValue() == HttpStatus.SC_CREATED) {
            return;
        } else {
            LOGGER.error("Invalid deploy result code: {} for url", objectResponseEntity.getStatusCode() + deployApiUrl);
            throw new InternalServerErrorException("Invalid deploy result code: " + objectResponseEntity.getStatusCode());
        }
    }

    protected String encode(String string) {
        if (string != null) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
            }
        }
        return null;
    }
}
