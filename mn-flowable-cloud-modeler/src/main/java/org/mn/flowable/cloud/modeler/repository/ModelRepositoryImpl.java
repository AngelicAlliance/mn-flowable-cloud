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
package org.mn.flowable.cloud.modeler.repository;

import org.mn.flowable.cloud.common.util.TenantProvider;
import org.mn.flowable.cloud.common.util.UuidIdGenerator;
import org.mn.flowable.cloud.modeler.domain.Model;
import org.mn.flowable.cloud.modeler.mapper.ModelMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModelRepositoryImpl implements ModelRepository {

    private static final String NAMESPACE = "org.mn.flowable.cloud.modeler.mapper.ModelMapper.";
    @Autowired
    protected SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    protected UuidIdGenerator idGenerator;
    @Autowired
    protected TenantProvider tenantProvider;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Model get(String id) {
        return modelMapper.selectModelById(id);
        //return sqlSessionTemplate.selectOne(NAMESPACE + "selectModel", id);
    }

    @Override
    public List<Model> findByModelType(Integer modelType, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("modelType", modelType);
        params.put("sort", sort);
        return findModelsByParameters(params);
    }

    @Override
    public List<Model> findByModelTypeAndFilter(Integer modelType, String filter, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("modelType", modelType);
        params.put("filter", filter);
        params.put("sort", sort);
        return findModelsByParameters(params);
    }

    @Override
    public List<Model> findByKeyAndType(String key, Integer modelType) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        params.put("modelType", modelType);
        return findModelsByParameters(params);
    }

    @Override
    public List<Model> findByParentModelId(String parentModelId) {
        return modelMapper.selectModelByParentModelId(parentModelId);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelByParentModelId", parentModelId);
    }

    @Override
    public Long countByModelTypeAndCreatedBy(int modelType, String createdBy) {
        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", createdBy);
        params.put("modelType", modelType);
        params.put("tenantId", tenantProvider.getTenantId());
        return modelMapper.countByModelTypeAndCreatedBy(params);
        //return sqlSessionTemplate.selectOne(NAMESPACE + "countByModelTypeAndCreatedBy", params);
    }

    protected List<Model> findModelsByParameters(Map<String, Object> parameters) {
        parameters.put("tenantId", tenantProvider.getTenantId());

        return modelMapper.selectModelByParameters(parameters);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelByParameters", parameters);
    }

    @Override
    public void save(Model model) {
        model.setTenantId(tenantProvider.getTenantId());
        if (model.getId() == null) {
            model.setId(idGenerator.generateId());
            modelMapper.insertModel(model);
            //sqlSessionTemplate.insert(NAMESPACE + "insertModel", model);
        } else {
            modelMapper.updateModel(model);
            //sqlSessionTemplate.update(NAMESPACE + "updateModel", model);
        }
    }

    @Override
    public void delete(Model model) {
        modelMapper.deleteModel(model);
        //sqlSessionTemplate.delete(NAMESPACE + "deleteModel", model);
    }

}
