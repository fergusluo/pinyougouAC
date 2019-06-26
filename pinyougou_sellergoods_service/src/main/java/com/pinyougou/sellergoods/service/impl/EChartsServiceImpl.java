package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.sellergoods.service.EChartsService;
import entity.ECharts;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:31:07
 */
@Service
public class EChartsServiceImpl implements EChartsService {
    @Autowired
    private TbOrderMapper orderMapper;

    //@Override
    //public ECharts findSalesCharts(String sellerName, String startDateStr, String endDateStr) {
    //    Date startDate = new Date(startDateStr);
    //    Date endDate = new Date(endDateStr);
    //    Example example = new Example(TbOrder.class);
    //    Example.Criteria criteria = example.createCriteria();
    //    criteria.andBetween("paymentTime", startDate, endDate);
    //    //criteria.andEqualTo("status","2");
    //    example.setOrderByClause("paymentTime");
    //    List<TbOrder> orderList = orderMapper.selectByExample(example);
    //    List<String> day = new ArrayList<>();
    //    List<Double> salesCount = new ArrayList<>();
    //    ECharts eCharts = new ECharts();
    //    for (TbOrder order : orderList) {
    //        double payment = order.getPayment().doubleValue();
    //        Date paymentTime = order.getPaymentTime();
    //        String pTime = paymentTime.getMonth() + 1 + "." + paymentTime.getDate();
    //        if (day.size()>0 && day.get(day.size() - 1).equals(pTime)) {
    //            salesCount.set(day.size()-1,salesCount.get(salesCount.size() - 1) + payment);
    //        } else {
    //            day.add(pTime);
    //            salesCount.add(payment);
    //        }
    //    }
    //    eCharts.setDay(day);
    //    eCharts.setSalesCount(salesCount);
    //    return eCharts;
    //}

    @Override
    public ECharts findSalesCharts(String sellerName, String startDateStr, String endDateStr) {
        try {
            ECharts eCharts = new ECharts();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sf.parse(startDateStr);
            Date endDate = sf.parse(endDateStr);
            Date temp = null;
            long dayCount = endDate.getTime() - startDate.getTime();//获取2个date之间的时间差
            long ms = 1000;//防止在大位数的int运算时发生溢出
            int days = (int) (dayCount / (24 * 60 * 60 * ms));//算出2个date之间的天数
            List<String> day = new ArrayList<>();
            List<Double> salesCount = new ArrayList<>();
            for (int i = 0; i <= days; i++) {
                temp = new Date(startDate.getTime() + (i * 24 * 60 * 60 * ms));
                String format = sf.format(temp);
                //String tempStr = temp.getYear()+"-"+temp.getMonth()+"-"+temp.getDate();
                day.add(format);
            }
            for (String everyDay : day) {
                Example example = new Example(TbOrder.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andBetween("paymentTime",everyDay + " 00:00:00",everyDay + " 23:59:59");
                //criteria.andLike("paymentTime", everyDay);
                //criteria.andEqualTo("status","2");
                //example.setOrderByClause("paymentTime");
                List<TbOrder> orderList = orderMapper.selectByExample(example);
                if (orderList == null) {
                    salesCount.add(0.0);
                } else {
                    double payment = 0.0;
                    for (TbOrder order : orderList) {
                        payment += order.getPayment().doubleValue();
                    }
                    salesCount.add(payment);
                }
            }
            eCharts.setDay(day);
            eCharts.setSalesCount(salesCount);
            return eCharts;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ECharts();
    }
}



