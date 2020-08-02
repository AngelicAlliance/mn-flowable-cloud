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
package org.mn.flowable.cloud.common.service.idm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpStatus;
import org.mn.flowable.cloud.common.model.RemoteGroup;
import org.mn.flowable.cloud.common.model.RemoteToken;
import org.mn.flowable.cloud.common.model.RemoteUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@Primary
public class JeecgIdmServiceImpl implements RemoteIdmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JeecgIdmServiceImpl.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestTemplate restTemplate;

    protected String url;
    protected String adminUser;
    protected String adminPassword;

    public JeecgIdmServiceImpl() {
        //url = properties.determineIdmAppUrl();
        url = "http://jeecg-cloud-system-biz";
        adminUser = "admin";
        adminPassword = "password";
    }

    @Override
    public RemoteUser authenticateUser(String username, String password) {
        JsonNode json = callRemoteIdmService(url + "api/idm/users/" + encode(username), username, password);
        if (json != null) {
            return parseUserInfo(json);
        }
        return null;
    }

    @Override
    public RemoteToken getToken(String tokenValue) {
        JsonNode json = callRemoteIdmService(url + "api/idm/tokens/" + encode(tokenValue), adminUser, adminPassword);
        if (json != null) {
            RemoteToken token = new RemoteToken();
            token.setId(json.get("id").asText());
            token.setValue(json.get("value").asText());
            token.setUserId(json.get("userId").asText());
            return token;
        }
        return null;
    }

    @Override
    public RemoteUser getUser(String userId) {
        RemoteUser remoteUser = new RemoteUser();

        JsonNode json;
        json = callRemoteIdmService(url + "/sys/user/info/" + encode(userId), adminUser, adminPassword);
        if (json != null) {
            JsonNode body = json.get("result");
            remoteUser = parseUserInfo(body);
        }
        json = callRemoteIdmService(url + "/sys/user/rolesSet/" + encode(userId), adminUser, adminPassword);
        if (json != null) {
            JsonNode body = json.get("result");
            remoteUser.setGroups(parseGroupsInfo(body));
        }
        json = callRemoteIdmService(url + "/sys/user/permissionsSet/" + encode(userId), adminUser, adminPassword);
        if (json != null) {
            JsonNode body = json.get("result");
            //remoteUser = parseUserInfo(body);
            remoteUser.setPrivileges(parsePrivilegesInfo(json));
        }
        remoteUser.getPrivileges().addAll(
                Arrays.asList("access-idm", "access-rest-api", "access-task", "access-modeler", "access-admin")
        );

        return remoteUser;
    }

    @Override
    public List<RemoteUser> findUsersByNameFilter(String filter) {
        JsonNode json = callRemoteIdmService(url + "/sys/user/list?username=" + encode(filter), adminUser, adminPassword);
        if (json != null) {
            return parseUsersInfo(json.get("result").get("records"));
        }
        return new ArrayList<>();
    }

    @Override
    public List<RemoteUser> findUsersByGroup(String groupId) {
        JsonNode json = callRemoteIdmService(url + "api/idm/groups/" + encode(groupId) + "/users", adminUser, adminPassword);
        if (json != null) {
            return parseUsersInfo(json);
        }
        return new ArrayList<>();
    }

    @Override
    public RemoteGroup getGroup(String groupId) {
        JsonNode json = callRemoteIdmService(url + "/sys/role/queryById?id=" + encode(groupId), adminUser, adminPassword);
        if (json != null) {
            return parseGroupInfo(json);
        }
        return null;
    }

    @Override
    public List<RemoteGroup> findGroupsByNameFilter(String filter) {
        JsonNode json = callRemoteIdmService(url + "/sys/role/list?roleName=" + encode(filter), adminUser, adminPassword);
        if (json != null) {
            return parseGroupsInfo(json);
        }
        return new ArrayList<>();
    }

    protected JsonNode callRemoteIdmService(String url, String username, String password) {
        URI uri = URI.create(url);
        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(
                        Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8))))
                .build();
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        if (responseEntity.getStatusCode().value() == HttpStatus.SC_OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    protected List<RemoteUser> parseUsersInfo(JsonNode json) {
        List<RemoteUser> result = new ArrayList<>();
        if (json != null && json.isArray()) {
            ArrayNode array = (ArrayNode) json;
            for (JsonNode userJson : array) {
                result.add(parseUserInfo(userJson));
            }
        }
        return result;
    }

    protected RemoteUser parseUserInfo(JsonNode json) {
        RemoteUser user = new RemoteUser();
        user.setId(json.get("username").asText());
        user.setFirstName(json.get("username").asText());
        user.setLastName(json.get("username").asText());
        //if (json.has("displayName") && !json.get("displayName").isNull()) {
        user.setDisplayName(json.get("username").asText());
        //}
        user.setEmail(json.get("email").asText());
        user.setFullName(json.get("username").asText());
        if (json.has("tenantId") && !json.get("tenantId").isNull()) {
            user.setTenantId(json.get("tenantId").asText());
        }
        return user;
    }

    protected List<RemoteGroup> parseGroupsInfo(JsonNode json) {
        List<RemoteGroup> result = new ArrayList<>();
        if (json != null && json.isArray()) {
            ArrayNode array = (ArrayNode) json;
            for (JsonNode userJson : array) {
                result.add(parseGroupInfo(userJson));
            }
        }
        return result;
    }

    protected RemoteGroup parseGroupInfo(JsonNode json) {
        RemoteGroup group = new RemoteGroup();
        group.setId(json.asText());
        group.setName(json.asText());
        return group;
    }

    protected List<String> parsePrivilegesInfo(JsonNode json) {
        List<String> result = new ArrayList<>();
        if (json != null && json.isArray()) {
            ArrayNode array = (ArrayNode) json;
            for (JsonNode privilegeJson : array) {
                result.add(privilegeJson.asText());
            }
        }
        return result;
    }

    protected String encode(String s) {
        if (s == null) {
            return "";
        }

        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            LOGGER.warn("Could not encode url param", e);
            return null;
        }
    }

}
