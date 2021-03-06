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
import org.mn.flowable.cloud.modeler.domain.ModelHistory;
import org.mn.flowable.cloud.modeler.mapper.ModelHistoryMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModelHistoryRepositoryImpl implements ModelHistoryRepository {

    private static final String NAMESPACE = "org.mn.flowable.cloud.modeler.mapper.ModelHistoryMapper.";
    @Autowired
    protected SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    protected UuidIdGenerator idGenerator;
    @Autowired
    protected TenantProvider tenantProvider;
    @Autowired
    ModelHistoryMapper modelHistoryMapper;

    @Override
    public ModelHistory get(String id) {
        return modelHistoryMapper.selectModelHistory(id);
        //return sqlSessionTemplate.selectOne(NAMESPACE + "selectModelHistory", id);
    }

    public List<ModelHistory> findByModelTypAndCreatedBy(String createdBy, Integer modelType) {
        Map<String, Object> params = new HashMap<>();
        params.put("modelType", modelType);
        params.put("createdBy", createdBy);
        params.put("tenantId", tenantProvider.getTenantId());
        return modelHistoryMapper.selectModelHistoryByTypeAndCreatedBy(params);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelHistoryByTypeAndCreatedBy", params);
    }

    public List<ModelHistory> findByModelId(String modelId) {
        return modelHistoryMapper.selectModelHistoryByModelId(modelId);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelHistoryByModelId", modelId);
    }

    @Override
    public void save(ModelHistory modelHistory) {
        modelHistory.setTenantId(tenantProvider.getTenantId());
        if (modelHistory.getId() == null) {
            modelHistory.setId(idGenerator.generateId());
            modelHistoryMapper.insertModelHistory(modelHistory);
            //sqlSessionTemplate.insert(NAMESPACE + "insertModelHistory", modelHistory);
        } else {
            modelHistoryMapper.updateModelHistory(modelHistory);
            //sqlSessionTemplate.update(NAMESPACE + "updateModelHistory", modelHistory);
        }
    }

    @Override
    public void delete(ModelHistory modelHistory) {
        modelHistoryMapper.deleteModelHistory(modelHistory);
        //sqlSessionTemplate.delete(NAMESPACE + "deleteModelHistory", modelHistory);
    }

}
