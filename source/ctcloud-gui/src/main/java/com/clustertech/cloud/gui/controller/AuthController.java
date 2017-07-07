package com.clustertech.cloud.gui.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.clustertech.cloud.gui.domain.UserInfoEntity;

/*
 * This controller is used for testing login, it will be delete in the future.
 */
@RestController
@RequestMapping("/api")
public class AuthController extends BaseController<AuthController> implements Serializable {

    private static final long serialVersionUID = -5267637601956676022L;

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public Map<String, Object> auth() {
        Map<String, Object> result = new HashMap<String, Object>();

        List<String> permissionist = new ArrayList<String>();
        permissionist.add("vm:create");
        List<String> roleList = new ArrayList<String>();
        roleList.add("admin");

        UserInfoEntity user = new UserInfoEntity();
        user.setId(1);
        user.setUsername("admin");
        user.setRoles(roleList);
        user.setPermissions(permissionist);

        result.put("userInfo", user);
        result.put("accessToken", "test_access_token");
        result.put("refreshToken", "test_refresh_token");

        return result;
    }
}
