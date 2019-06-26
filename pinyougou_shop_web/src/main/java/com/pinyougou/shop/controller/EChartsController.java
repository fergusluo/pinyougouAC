package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.sellergoods.service.EChartsService;
import entity.ECharts;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:15:55
 */
@RestController
@RequestMapping("echarts")
public class EChartsController {
    @Reference
    EChartsService eChartsService;
    @RequestMapping("findSalesCharts")
    public ECharts findSalesCharts(String startDateStr, String endDateStr) {
        String sellerName = SecurityContextHolder.getContext().getAuthentication().getName();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        if (!sellerName.equals("anonymousUser")) {
            if (startDateStr == null || endDateStr == null || startDateStr.equals("") || endDateStr.equals("")) {
                endDateStr = sf.format(new Date());
                Date startDate = new Date();
                startDate.setMonth(startDate.getMonth()-1);
                startDateStr = sf.format(startDate);
            }
            ECharts salesCharts = eChartsService.findSalesCharts(sellerName, startDateStr, endDateStr);
            return salesCharts;
        }
        return new ECharts();
    }
}
