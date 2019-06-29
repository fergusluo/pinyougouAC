package com.pinyougou.sellergoods.service;

import entity.ECharts;
import entity.PieCharts;

import java.util.List;
import java.util.Map;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:25:00
 */
public interface EChartsService {
    ECharts findSalesCharts(String sellerName,String startDateStr,String endDateStr);

    Map<String,Object> findSalesItemsPieCharts();
}
