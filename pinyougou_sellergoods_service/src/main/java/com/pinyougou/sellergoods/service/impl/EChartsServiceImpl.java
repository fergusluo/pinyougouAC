package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.abel533.mapper.Mapper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.EChartsService;
import entity.ECharts;
import entity.PieCharts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:31:07
 */
@Service
public class EChartsServiceImpl implements EChartsService {
    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public ECharts findSalesCharts(String sellerId, String startDateStr, String endDateStr) {
        try {
            ECharts eCharts = new ECharts();
            //查询从起始日期至终止日期之间的每一天
            List<String> day = createDayList(startDateStr, endDateStr);
            //根据createDayList()的结果查询每一天销售额
            List<Double> salesCount = getSalesCount(day, sellerId);
            eCharts.setDay(day);
            eCharts.setSalesCount(salesCount);
            return eCharts;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ECharts();
    }

    @Override
    public Map<String, Object> findSalesItemsPieCharts() {
        //新建返回值
        Map<String, Object> map = new HashMap<>();
        List<PieCharts> categoryList = new ArrayList<>();
        List<String> categoryNameList = new ArrayList<>();
        try {
            //查询payLog表,根据payTime获取支付成功日志
            Example payLogExample = new Example(TbPayLog.class);
            Example.Criteria payLogCriteria = payLogExample.createCriteria();
            //限制查询支付时间
            SimpleDateFormat payTimeSF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            payLogCriteria.andBetween("payTime", payTimeSF.parse("2019-01-01 00:00:00"), payTimeSF.format(new Date()));
            List<TbPayLog> payLogs = payLogMapper.selectByExample(payLogExample);

            //获取payLogs内的所有orderList
            List<Object> orderIds = new ArrayList<>();
            for (TbPayLog payLog : payLogs) {
                orderIds.add(payLog.getOrderList());
            }

            //根据orderIds查询orderItem表商品goodsId
            Example orderItemExample = new Example(TbOrderItem.class);
            Example.Criteria orderItemCriteria = orderItemExample.createCriteria();
            orderItemCriteria.andIn("orderId", orderIds);
            List<TbOrderItem> orderItems = orderItemMapper.selectByExample(orderItemExample);

            //获取orderItems内所有goodsId
            List<Object> goodsIds = new ArrayList<>();
            for (TbOrderItem orderItem : orderItems) {
                goodsIds.add(orderItem.getGoodsId());
            }

            //根据goodsId查询goods表商品
            List<TbGoods> goods = new ArrayList<>();
            for (Object goodsId : goodsIds) {
                TbGoods goodsTemp = goodsMapper.selectByPrimaryKey(goodsId);
                goods.add(goodsTemp);
            }

            //获取goods内所有一级分类category1Id
            List<Long> category1Ids = new ArrayList<>();
            for (TbGoods good : goods) {
                category1Ids.add(good.getCategory1Id());
            }

            //category1Ids去重
            Set<Long> category1IdSet = new HashSet<>();
            category1IdSet.addAll(category1Ids);
            List<Long> category1IdList = new ArrayList<>();
            category1IdList.addAll(category1IdSet);

            //category1Ids累加
            for (Long tempId : category1IdList) {
                long countTemp = 0L;
                for (Long id : category1Ids) {
                    if (tempId.equals(id)) {
                        countTemp = countTemp + 1L;
                    }
                }
                PieCharts pieCharts = new PieCharts();
                String categoryName = itemCatMapper.selectByPrimaryKey(tempId).getName();
                pieCharts.setName(categoryName);
                pieCharts.setValue(countTemp + "");
                categoryList.add(pieCharts);
            }

            //从大到小排序
            categoryList.sort(new Comparator<PieCharts>() {
                @Override
                public int compare(PieCharts o1, PieCharts o2) {
                    return Integer.parseInt(o2.getValue()) - Integer.parseInt(o1.getValue());
                }
            });

            //存入排行前五名称
            if (categoryList.size() >= 5) {
                for (int i = 0; i < 5; i++) {
                    categoryNameList.add(categoryList.get(i).getName());
                }
            } else {
                for (PieCharts charts : categoryList) {
                    categoryNameList.add(charts.getName());

                }

            }

            //存入返回值map
            map.put("categoryList", categoryList);
            map.put("categoryNameList", categoryNameList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return map;
    }

    //查询从起始日期至终止日期之间的每一天
    private List<String> createDayList(String startDateStr, String endDateStr) throws ParseException {
        SimpleDateFormat dateSF = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateSF.parse(startDateStr);
        Date endDate = dateSF.parse(endDateStr);
        Date temp = null;
        long dayCount = endDate.getTime() - startDate.getTime();//获取2个date之间的时间差
        long ms = 1000;//防止在大位数的int运算时发生溢出
        int days = (int) (dayCount / (24 * 60 * 60 * ms));//算出2个date之间的天数
        List<String> day = new ArrayList<>();
        for (int i = 0; i <= days; i++) {
            temp = new Date(startDate.getTime() + (i * 24 * 60 * 60 * ms));
            String format = dateSF.format(temp);
            day.add(format);
        }
        return day;
    }

    //根据createDayList()的结果查询每一天销售额
    private List<Double> getSalesCount(List<String> day, String sellerId) {
        List<Double> salesCount = new ArrayList<>();
        for (String everyDay : day) {
            Example example = new Example(TbOrder.class);
            Example.Criteria criteria = example.createCriteria();
            //限制查询支付时间
            criteria.andBetween("paymentTime", everyDay + " 00:00:00", everyDay + " 23:59:59");
            //限制查询status=2已完成支付订单
            criteria.andEqualTo("status", "2");
            //若为sellerId=null为运营商登录,不限制商家查询销售额
            //若为商家Id,则查询该商家自己的销售额
            if (sellerId != null && !"".equals(sellerId)) {
                criteria.andEqualTo("sellerId", sellerId);
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



