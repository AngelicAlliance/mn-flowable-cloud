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
import org.mn.flowable.cloud.modeler.domain.Model;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Mapper
@Component
public interface ModelMapper {

    void insertModel(Model model);

    void updateModel(Model model);

    Model selectModelById(String id);

    List<Model> selectModelByParentModelId(String parentModelId);

    List<Model> selectModelByParameters(Map map);

    void deleteModel(Model model);

    Long countByModelTypeAndCreatedBy(Map map);

}
