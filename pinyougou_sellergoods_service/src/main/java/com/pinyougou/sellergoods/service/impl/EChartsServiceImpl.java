package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.sellergoods.service.EChartsService;
import entity.ECharts;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Override
    public ECharts findSalesCharts(String sellerId, String startDateStr, String endDateStr) {
        try {
            ECharts eCharts = new ECharts();
            //查询从起始日期至终止日期之间的每一天
            List<String> day = createDayList(startDateStr, endDateStr);
            //根据createDayList()的结果查询每一天销售额
            List<Double> salesCount = getSalesCount(day,sellerId);
            eCharts.setDay(day);
            eCharts.setSalesCount(salesCount);
            return eCharts;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ECharts();
    }

    //查询从起始日期至终止日期之间的每一天
    private List<String> createDayList(String startDateStr, String endDateStr) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sf.parse(startDateStr);
        Date endDate = sf.parse(endDateStr);
        Date temp = null;
        long dayCount = endDate.getTime() - startDate.getTime();//获取2个date之间的时间差
        long ms = 1000;//防止在大位数的int运算时发生溢出
        int days = (int) (dayCount / (24 * 60 * 60 * ms));//算出2个date之间的天数
        List<String> day = new ArrayList<>();
        for (int i = 0; i <= days; i++) {
            temp = new Date(startDate.getTime() + (i * 24 * 60 * 60 * ms));
            String format = sf.format(temp);
            day.add(format);
        }
        return day;
    }

    //根据createDayList()的结果查询每一天销售额
    private List<Double> getSalesCount(List<String> day,String sellerId) {
        List<Double> salesCount = new ArrayList<>();
        for (String everyDay : day) {
            Example example = new Example(TbOrder.class);
            Example.Criteria criteria = example.createCriteria();
            //限制查询支付时间
            criteria.andBetween("paymentTime",everyDay + " 00:00:00",everyDay + " 23:59:59");
            //限制查询status=2已完成支付订单
            criteria.andEqualTo("status","2");
            //若为sellerId=null为运营商登录,不限制商家查询销售额
            //若为商家Id,则查询该商家自己的销售额
            if (sellerId!=null&&!"".equals(sellerId)){
                criteria.andEqualTo("sellerId",sellerId);
            }
            List<TbOrder> orderList = orderMapper.selectByExample(example);
            if (orderList == null) {
                //当天无销售额,插入0.0,防止后续时间与销售额对应错误
                salesCount.add(0.0);
            } else {
                //当天多笔销售额,则累加
                double payment = 0.0;
                for (TbOrder order : orderList) {
                    payment += order.getPayment().doubleValue();
                }
                salesCount.add(payment);
            }
        }
        return salesCount;
    }

}



