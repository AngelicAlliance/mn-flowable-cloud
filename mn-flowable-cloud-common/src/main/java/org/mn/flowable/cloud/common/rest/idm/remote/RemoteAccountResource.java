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
package org.mn.flowable.cloud.common.rest.idm.remote;

import org.mn.flowable.cloud.common.model.GroupRepresentation;
import org.mn.flowable.cloud.common.model.RemoteGroup;
import org.mn.flowable.cloud.common.model.RemoteUser;
import org.mn.flowable.cloud.common.model.UserRepresentation;
import org.mn.flowable.cloud.common.security.SecurityUtils;
import org.mn.flowable.cloud.common.service.exception.NotFoundException;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app")
public class RemoteAccountResource {

    @Autowired
    private RemoteIdmService remoteIdmService;

    /**
     * GET /rest/account -> get the current user.
     */
    @GetMapping(value = "/rest/account", produces = "application/json")
    public UserRepresentation getAccount() {
        UserRepresentation userRepresentation = null;
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            RemoteUser remoteUser = remoteIdmService.getUser(currentUserId);
            if (remoteUser != null) {
                userRepresentation = new UserRepresentation(remoteUser);

                if (remoteUser.getGroups() != null && remoteUser.getGroups().size() > 0) {
                    List<GroupRepresentation> groups = new ArrayList<>();
                    for (RemoteGroup remoteGroup : remoteUser.getGroups()) {
                        groups.add(new GroupRepresentation(remoteGroup));
                    }
                    userRepresentation.setGroups(groups);
                }

                if (remoteUser.getPrivileges() != null && remoteUser.getPrivileges().size() > 0) {
                    userRepresentation.setPrivileges(remoteUser.getPrivileges());
                }

            }
        }

        if (userRepresentation != null) {
            return userRepresentation;
        } else {
            throw new NotFoundException();
        }
    }

}
