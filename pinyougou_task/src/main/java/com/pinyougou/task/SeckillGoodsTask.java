package com.pinyougou.task;

import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * SpringTask入门
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.task
 * @date 2019-6-15
 */
@Component
public class SeckillGoodsTask {

    //每秒都执行一次
    //秒 分 小时 月份中的日期 月份 星期中的日期 年份
    //@Scheduled(cron = "0-5,15-18 * * * * ?")
    public void startTask(){
        System.out.println("定时器执行了，startTask方法：" + new Date());
    }

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //30秒执行一次
    @Scheduled(cron = "*/5 * * * * ?")
    public void refreshSeckillGoods(){
        //查询附合条件的商品列表
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //状态已审核
        criteria.andEqualTo("status", 1);
        //有库存
        criteria.andGreaterThan("stockCount", 0);
        //在活动时间表范围内：start_time <= NOW() <= end_time
        Date now = new Date();
        //开始时间小于等于当前时间
        criteria.andLessThanOrEqualTo("startTime", now);
        //结时间要大于等于当前时间
        criteria.andGreaterThanOrEqualTo("endTime", now);

        //排除redis中已有的数据
        Set ids = redisTemplate.boundHashOps("seckillGoods").keys();
        if(ids != null && ids.size() > 0){
            //Set转list
            List idList = new ArrayList(ids);
            criteria.andNotIn("id", idList);
        }

        //查询商品列表
        List<TbSeckillGoods> goodsList = seckillGoodsMapper.selectByExample(example);
        if(goodsList != null && goodsList.size() > 0) {
            //放入redis
            for (TbSeckillGoods goods : goodsList) {
                System.out.println("定时任务添加了商品进入redis，商品id为：" + goods.getId());
                //以商品id为key，商品信息为value
                redisTemplate.boundHashOps("seckillGoods").put(goods.getId(), goods);

                //increment(操作的key,操作的值(可以是负数))
                redisTemplate.boundHashOps("seckillGoodsStockCount").increment(goods.getId(), goods.getStockCount());
            }
        }else{
            System.out.println("此次定时任务，没有找到附合条件的商品信息...");
        }
    }
}
