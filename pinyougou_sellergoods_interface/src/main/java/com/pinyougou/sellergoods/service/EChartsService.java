package com.pinyougou.sellergoods.service;

import entity.ECharts;

import java.util.List;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:25:00
 */
public interface EChartsService {
    ECharts findSalesCharts(String sellerName,String startDateStr,String endDateStr);
}
