package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.user.controller
 * @date 2019-6-9
 */
@RestController
@RequestMapping("login")
public class LoginController {

    @RequestMapping("info")
    public Map info(){
        Map map = new HashMap();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", userName);
        return map;
    }
}
