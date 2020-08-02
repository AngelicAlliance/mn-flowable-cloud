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

import org.mn.flowable.cloud.common.util.UuidIdGenerator;
import org.mn.flowable.cloud.modeler.domain.ModelInformation;
import org.mn.flowable.cloud.modeler.domain.ModelRelation;
import org.mn.flowable.cloud.modeler.mapper.ModelRelationMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModelRelationRepositoryImpl implements ModelRelationRepository {

    private static final String NAMESPACE = "org.mn.flowable.cloud.modeler.mapper.ModelRelationMapper.";
    @Autowired
    protected SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    protected UuidIdGenerator idGenerator;
    @Autowired
    ModelRelationMapper modelRelationMapper;

    @Override
    public List<ModelRelation> findByParentModelIdAndType(String parentModelId, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("parentModelId", parentModelId);
        params.put("type", type);
        return modelRelationMapper.selectModelRelationByParentModelIdAndType(params);
        //return sqlSessionTemplate.selectList("selectModelRelationByParentModelIdAndType", params);
    }

    @Override
    public List<ModelInformation> findModelInformationByParentModelId(String parentModelId) {
        return modelRelationMapper.selectModelInformationByParentModelId(parentModelId);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelInformationByParentModelId", parentModelId);
    }

    @Override
    public List<ModelInformation> findModelInformationByChildModelId(String modelId) {
        return modelRelationMapper.selectModelInformationModelId(modelId);
        //return sqlSessionTemplate.selectList(NAMESPACE + "selectModelInformationModelId", modelId);
    }

    @Override
    public void deleteModelRelationsForParentModel(String parentModelId) {
        modelRelationMapper.deleteModelRelationByParentModelId(parentModelId);
        //sqlSessionTemplate.delete(NAMESPACE + "deleteModelRelationByParentModelId", parentModelId);
    }

    @Override
    public void save(ModelRelation modelRelation) {
        if (modelRelation.getId() == null) {
            modelRelation.setId(idGenerator.generateId());
            modelRelationMapper.insertModelRelation(modelRelation);
            //sqlSessionTemplate.insert(NAMESPACE + "insertModelRelation", modelRelation);
        } else {
            modelRelationMapper.updateModelRelation(modelRelation);
            //sqlSessionTemplate.update(NAMESPACE + "updateModelRelation", modelRelation);
        }
    }

    @Override
    public void delete(ModelRelation modelRelation) {
        modelRelationMapper.deleteModelRelation(modelRelation);
        // sqlSessionTemplate.delete(NAMESPACE + "deleteModelRelation", modelRelation);
    }

}
