package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.IdWorker;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
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
    private OrderService orderService;

    //生成二维码
    @RequestMapping("createNative")
    public Map createNative() {
        /*IdWorker idWorker = new IdWorker();
        String out_trade_no = idWorker.nextId() + "";
        //我们先测试1分钱
        return weixinPayService.createNative(out_trade_no, "1");*/

        //使用支付日志后的流程
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
    }

    //查询订单支付状态
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
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

                    //更新日志状态与订单状态
                    orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));

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
            if(i > 100){
                return new Result(false, "支付已超时");
            }
        }
    }
}
