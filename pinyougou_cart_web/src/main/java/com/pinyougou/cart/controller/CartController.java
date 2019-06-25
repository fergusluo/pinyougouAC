package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.CookieUtil;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2019-6-11
 */
@RestController
@RequestMapping("cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 查询当前用户的购物车列表
     * @return
     */
    /*@RequestMapping("findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Cart> cartList = null;
        //从cookie中读取购物车数据
        String json = CookieUtil.getCookieValue(request, "cartList", true);
        //如果cookie没有数据
        if(json == null || json.length() < 1){
            cartList = new ArrayList<>();
        }else{
            cartList = JSON.parseArray(json, Cart.class);
        }
        return cartList;
    }*/

    /**
     * 未合并购物前的逻辑
     * 查询当前用户的购物车列表
     * @return
     */
    /*@RequestMapping("findCartList")
    public List<Cart> findCartList(){
        List<Cart> cartList = new ArrayList<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        //如果用户没登录
        if("anonymousUser".equals(username)){
            System.out.println("从cookie中读取了购物车数据...");
            //从cookie中读取购物车数据
            String json = CookieUtil.getCookieValue(request, "cartList", true);
            cartList = JSON.parseArray(json, Cart.class);
        }else{
            System.out.println("从redis中读取了购物车数据...");
            cartList = cartService.findCartListFromRedis(username);
        }
        return cartList;
    }*/

    /**
     * 合并购物车后的逻辑
     * 查询当前用户的购物车列表
     * @return
     */
    @RequestMapping("findCartList")
    public List<Cart> findCartList(){
        List<Cart> cartList = new ArrayList<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //从cookie中读取购物车数据
        String json = CookieUtil.getCookieValue(request, "cartList", true);
        if(json != null && json.length() > 0){
            cartList = JSON.parseArray(json, Cart.class);
        }
        //如果用户没登录
        if("anonymousUser".equals(username)){
            System.out.println("从cookie中读取了购物车数据...");
        }else{
            System.out.println("从redis中读取了购物车数据...");
            //查询redis中的购物车
            List<Cart> cartListRedis = cartService.findCartListFromRedis(username);
            //如果cookie中有购物车数据
            if(cartList.size() > 0){
                System.out.println("合并购物车...");
                //合并两组购物车
                cartList = cartService.mergeCartList(cartList, cartListRedis);
                //把合并后的购物车保存到redis中
                cartService.saveCartListToRedis(username, cartList);
                //清空cookie中的购物车
                CookieUtil.deleteCookie(request, response, "cartList");
            }else{
                //返回登录版购物车
                cartList = cartListRedis;
            }
        }
        return cartList;
    }

    /**
     * 操作购物车逻辑
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:8085",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num){
        try {
            //设置可以访问的域，值设置为*时，允许所有域
            //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8085");
            //如果需要操作cookies，必须加上此配置，标识服务端可以写cookies，
            // 并且Access-Control-Allow-Origin不能设置为*，因为cookies操作需要域名
            //response.setHeader("Access-Control-Allow-Credentials", "true");

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //查询当前用户购物车
            List<Cart> cartList = this.findCartList();
            //注意此处调用的是dubbo远程接口，所以要重新接收一下
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            //如果用户没登录
            if("anonymousUser".equals(username)){
                System.out.println("操作了cookie的购物车数据...");
                //把商品信息保存到cookies中
                String value = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", value, 60 * 60 * 24, true);
            }else{
                System.out.println("操作了redis的购物车数据...");
                cartService.saveCartListToRedis(username,cartList);
            }
            return new Result(true, "操作成功！");
        } catch (RuntimeException e) {
            //提示系统service返回的消息
            return new Result(false, e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "操作失败！");
    }
}
