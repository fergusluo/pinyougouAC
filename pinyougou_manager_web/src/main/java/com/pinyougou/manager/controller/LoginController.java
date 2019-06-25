package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.manager.controller
 * @date 2019-5-25
 */
@RestController
@RequestMapping("login")
public class LoginController {

    @RequestMapping("info")
    public Map<String,Object> info(){
        Map<String, Object> map = new HashMap<>();
        //获取登录名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", userName);
        return map;
    }
}
