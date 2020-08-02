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
import org.mn.flowable.cloud.modeler.domain.ModelHistory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Mapper
@Component
public interface ModelHistoryMapper {

    void insertModelHistory(ModelHistory modelHistory);

    void updateModelHistory(ModelHistory modelHistory);

    ModelHistory selectModelHistory(String id);

    List<ModelHistory> selectModelHistoryByTypeAndCreatedBy(Map map);

    List<ModelHistory> selectModelHistoryByModelId(String historyId);

    void deleteModelHistory(ModelHistory modelHistory);
}
