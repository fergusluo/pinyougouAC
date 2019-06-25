package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 扩展权限认证类
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.service
 * @date 2019-5-25
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username + "用户，进入了UserDetailsServiceImpl.loadUserByUsername....");

        //构建角色列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //查询数据库的商家信息
        TbSeller seller = sellerService.getById(username);
        //只有审核通过的商家，才能登录
        if (seller != null && "1".equals(seller.getStatus())) {
            //返回数据库的密码
            return new User(username,seller.getPassword(),authorities);
        }else{
            //返回空对象表示认证失败
            return null;
        }

        //只要密码是123456，就放行
        //return new User(username,"123456",authorities);
    }
}
