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
package org.mn.flowable.cloud.modeler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.mn.flowable.cloud.modeler.domain.ModelInformation;
import org.mn.flowable.cloud.modeler.domain.ModelRelation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Mapper
@Component
public interface ModelRelationMapper {

    void deleteModelRelationByParentModelId(String parentModelId);

    void insertModelRelation(ModelRelation modelRelation);

    void updateModelRelation(ModelRelation modelRelation);

    void deleteModelRelation(ModelRelation modelRelation);

    List<ModelRelation> selectModelRelationByParentModelIdAndType(Map map);

    List<ModelInformation> selectModelInformationByParentModelId(String parentModelId);

    List<ModelInformation> selectModelInformationModelId(String modelId);

}
