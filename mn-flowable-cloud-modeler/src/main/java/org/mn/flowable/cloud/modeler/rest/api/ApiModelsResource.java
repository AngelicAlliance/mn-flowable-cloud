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
package org.mn.flowable.cloud.modeler.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mn.flowable.cloud.common.model.ResultListDataRepresentation;
import org.mn.flowable.cloud.common.security.SecurityUtils;
import org.mn.flowable.cloud.common.service.exception.BadRequestException;
import org.mn.flowable.cloud.modeler.domain.Model;
import org.mn.flowable.cloud.modeler.model.ModelKeyRepresentation;
import org.mn.flowable.cloud.modeler.model.ModelRepresentation;
import org.mn.flowable.cloud.modeler.service.FlowableModelQueryService;
import org.mn.flowable.cloud.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ApiModelsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiModelsResource.class);

    @Autowired
    protected FlowableModelQueryService modelQueryService;

    @Autowired
    protected ModelService modelService;

    @Autowired
    protected ObjectMapper objectMapper;

    @GetMapping(value = "/editor/models", produces = "application/json")
    public ResultListDataRepresentation getModels(@RequestParam(required = false) String filter, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer modelType,
                                                  HttpServletRequest request) {

        return modelQueryService.getModels(filter, sort, modelType, request);
    }

    @PostMapping(value = "/editor/import-process-model", produces = "application/json")
    public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return modelQueryService.importProcessModel(request, file);
    }

    @PostMapping(value = "/editor/models", produces = "application/json")
    public ModelRepresentation createModel(@RequestBody ModelRepresentation modelRepresentation) {
        modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));

        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(null, modelRepresentation.getModelType(), modelRepresentation.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Provided model key already exists: " + modelRepresentation.getKey());
        }

        String json = modelService.createModelJson(modelRepresentation);

        Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUserObject());
        return new ModelRepresentation(newModel);
    }

}
