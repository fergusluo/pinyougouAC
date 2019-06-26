package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.sellergoods.service.EChartsService;
import entity.ECharts;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:15:55
 */
@RestController
@RequestMapping("echarts")
public class EChartsController {
    @Reference
    EChartsService eChartsService;

    //根据时间查询登录商家销售额折线图
    @RequestMapping("findSalesCharts")
    public ECharts findSalesCharts(String startDateStr, String endDateStr) {
        //获取登录商家sellerId
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //判断日期是否为空,为空则默认设置从今日起前一月日期
        if (!sellerId.equals("anonymousUser")) {
            //判断日期是否为空,为空则默认设置从今日起前一月日期
            if (startDateStr == null || endDateStr == null || startDateStr.equals("") || endDateStr.equals("")) {
                endDateStr = sf.format(new Date());
                Date startDate = new Date();
                startDate.setMonth(startDate.getMonth()-1);
                startDateStr = sf.format(startDate);
            }
            ECharts salesCharts = eChartsService.findSalesCharts(sellerId, startDateStr, endDateStr);
            return salesCharts;
        }
        //未登录返回空
        return new ECharts();
    }
}
