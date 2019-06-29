package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import entity.PageResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {
    @Reference
    private OrderService orderService;

    @RequestMapping("findPage")
    public PageResult findPage(int pageNo, int pageSize, @RequestBody TbOrder order) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUserId(userId);
        return orderService.findPage(pageNo, pageSize,order);
    }
    @RequestMapping("getOrderItems")
    public List<TbOrderItem> getOrderItems(String[] ids) {
        List<TbOrderItem> list = new ArrayList<>();
        for (String id : ids) {
            long orderId = Long.parseLong(id);
            List<TbOrderItem> items = orderService.getOrderItems(orderId);
            list.addAll(items);
        }
        return list;
    }
}
