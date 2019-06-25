package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.service.impl
 * @date 2019-6-11
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品信息不存在！");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品下架！");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);  //商家id
            cart.setSellerName(item.getSeller()); //商家名称
            TbOrderItem orderItem = createOrderItem(item,num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //添加一个商品信息
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else { //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            //5.1. 如果没有，新增购物车商品明细
            if(orderItem == null){
                //追加一个商品信息
                orderItem = this.createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }else{ //5.2. 如果有，在原购物车明细上添加数量，更改金额
                //数量相加
                orderItem.setNum(orderItem.getNum() + num);
                double totalFee = 0.0;  //小计
                totalFee = item.getPrice().doubleValue() * orderItem.getNum();
                //重新计算小计
                orderItem.setTotalFee(new BigDecimal(totalFee));
                //如果修改数量后，当前要购物的数量少于1
                if(orderItem.getNum() < 1){
                    //删除当前商品信息
                    cart.getOrderItemList().remove(orderItem);

                    //如果删除商品信息后，当前商品列表中已经没有商品了
                    if(cart.getOrderItemList().size() < 1){
                        //删除整个商家购物车对象
                        cartList.remove(cart);
                    }
                }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList == null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //合并
                this.addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

   /* public static void main(String[] args) {
        Long a = 128L;
        Long b = 128L;
        //-127 到 127
        System.out.println(a.longValue() == b.longValue());
    }*/

    /**
     * 查询当前商家商品列表中有没有添加过用户要添加的商品
     * @param orderItemList 当前商家的商品列表
     * @param itemId 当前要添加购物车的商品id
     * @return 查找结果,null表示没有找到
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            //如果找到相应的商品信息
            if(orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车商品信息
     * @param item sku信息
     * @param num 购物的数量
     * @return 购物车商品信息
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        //先处理购买数量合法性
        if(num < 1){
            throw new RuntimeException("请输入正确的数量！");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);  //购买数量
        double totalFee = 0.0;  //小计
        totalFee = item.getPrice().doubleValue() * num;
        orderItem.setTotalFee(new BigDecimal(totalFee));
        orderItem.setTitle(item.getTitle());  //标题
        orderItem.setSellerId(item.getSellerId());
        orderItem.setPrice(item.getPrice());
        orderItem.setPicPath(item.getImage());  //图片
        orderItem.setGoodsId(item.getGoodsId());  //spuId
        orderItem.setItemId(item.getId());  //skuId
        return orderItem;
    }

    /**
     * 查询当前商家有没有添加过购物车
     * @param cartList 当前用户的购物车列表
     * @param sellerId 商家id
     * @return 查找的购物车信息,null代表没有找到
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            //如果当前商家添加过购物车
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}
