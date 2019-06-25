package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2019-6-14
 */
@RestController
@RequestMapping("pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    //生成二维码
    @RequestMapping("createNative")
    public Map createNative() {
        //查询当前用户未付款订单
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //金额（分）
        String fen =(long)(seckillOrder.getMoney().doubleValue() * 100) + "";
        //生成二维码
        return weixinPayService.createNative(seckillOrder.getId() + "", fen);
    }

    //查询订单支付状态
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        int i = 0;
        //循环查询订单信息
        while (true){
            //发起订单查询
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            if(map == null){
                return new Result(false, "支付失败！");
            }else {
                //如果订单已被支付
                if("SUCCESS".equals(map.get("trade_state"))){

                    //支付成功保存订单到数据库，清队订单缓存，更新排队标识
                    seckillOrderService.saveOrderFromRedisToDb(userId,map.get("transaction_id"));

                    return new Result(true, "支付成功!");
                }
            }
            try {
                //3秒发起一次请求
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            //300 / 3 = 100
            //超时控制
            if(i > 3){
                Result result = null;
                //1.调用微信的关闭订单接口
                Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
                if( !"SUCCESS".equals(payresult.get("result_code")) ){//如果返回结果是正常关闭
                    //如果订单已被支付
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        //正常发货
                        seckillOrderService.saveOrderFromRedisToDb(userId, map.get("transaction_id"));
                    }
                }
                if(result == null){
                    System.out.println("超时，取消订单");
                    //2.清除缓存
                    seckillOrderService.deleteOrderFromRedis(userId, new Long(out_trade_no));
                }
                //超时了，还原库存
                //seckillOrderService.deleteOrderFromRedis(userId,new Long(out_trade_no));
                return new Result(false, "支付已超时");
            }
        }
    }
}
