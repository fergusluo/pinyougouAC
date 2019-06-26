package com.pinyougou.manager.controller;

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

    //运营商根据时间查询所有商家销售额折线图
    @RequestMapping("findSalesCharts")
    public ECharts findSalesCharts(String startDateStr, String endDateStr) {
        //格式化日期
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //判断日期是否为空,为空则默认设置从今日起前一月日期
        if (startDateStr == null || endDateStr == null || startDateStr.equals("") || endDateStr.equals("")) {
            endDateStr = sf.format(new Date());
            Date startDate = new Date();
            startDate.setMonth(startDate.getMonth() - 1);
            startDateStr = sf.format(startDate);
        }
        //查询所有销售额,不需要传入商家名称
        ECharts salesCharts = eChartsService.findSalesCharts(null, startDateStr, endDateStr);
        return salesCharts;
    }
}
