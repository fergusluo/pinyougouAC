package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Order;
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
        PageResult<Order> page = orderService.findPage(pageNo, pageSize, order);
        //ArrayList<TbOrder> list = new ArrayList<>();
        //PageResult<TbOrder> pageResult = new PageResult<>();
        //for (Order row : page.getRows()) {
        //    list.add(row.getOrder());
        //}
        //pageResult.setPages(page.getPages());
        //pageResult.setRows(list);
        //return pageResult;
        return page;
    }

    @RequestMapping("getOrderItems")
    public List<TbOrderItem> getOrderItems(String[] ids) {
        List<TbOrderItem> list = new ArrayList<>();
        if (ids != null && ids.length > 0 )
            for (String id : ids) {
                if (!"".equals("")) {
                    long orderId = Long.parseLong(id);
                    List<TbOrderItem> items = orderService.getOrderItems(orderId);
                    list.addAll(items);
                }
            }
        return list;
    }
}
