package org.mn.flowable.cloud.common.feign;

import org.mn.flowable.cloud.common.model.RemoteGroup;
import org.mn.flowable.cloud.common.model.RemoteToken;
import org.mn.flowable.cloud.common.model.RemoteUser;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(value = "jeecg-cloud-system-biz")
@RequestMapping(value = "/api")
public interface JeecgIdmServiceClient extends RemoteIdmService {

    @GetMapping(value = "/idm/users/{userId}", produces = {"application/json"})
    RemoteUser authenticateUser(@PathVariable String username, @PathVariable String password);

    @RequestMapping(value = "/idm/tokens/{tokenValue}", method = RequestMethod.GET)
    RemoteToken getToken(@PathVariable String tokenValue);

    @GetMapping(value = "/idm/users/{userId}")
    RemoteUser getUser(@PathVariable String userId);

    @RequestMapping(value = "/idm/users", method = RequestMethod.GET, produces = {"application/json"})
    List<RemoteUser> findUsersByNameFilter(@RequestParam("filter") @Nullable String filter);

    @GetMapping(value = "/idm/groups/{groupId}/users")
    List<RemoteUser> findUsersByGroup(@PathVariable String groupId);

    @GetMapping(value = "/idm/groups/{groupId}")
    RemoteGroup getGroup(@PathVariable String groupId);

    @GetMapping(value = "/idm/groups")
    List<RemoteGroup> findGroupsByNameFilter(@RequestParam("filter") String filter);

}
