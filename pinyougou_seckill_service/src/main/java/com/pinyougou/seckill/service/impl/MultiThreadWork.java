package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.utils.IdWorker;
import entity.QueueTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * spring多线程入门
 *
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.seckill.service.impl
 * @date 2019-6-17
 */
@Component
@Transactional
public class MultiThreadWork {

    //异步，加入此注解后，此方法默认就是多线程的
    @Async
    public void doSomething(int i) {
        try {
            System.out.println(i + "我正在处理一些任务....." + new Date());
            Thread.sleep(3000);
            System.out.println(i + "任务已经处理完了，我很嗨皮....." + new Date());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate transRedisTemplate;
    /**
     * 多线程下单方法
     * @param seckillId 抢购的商品id
     */
    @Async
    public void createOrder(Long seckillId){
        try {
            System.out.println("模拟当前业务场景比较花时间，这里用时5秒.....");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //1、先把排队八信息中，最先排队的人取出来-右取
        String userId = (String) redisTemplate.boundListOps("seckill_goods_order_queue_" + seckillId).rightPop();

        //2.先扣减库存
        Long count = redisTemplate.boundHashOps("seckillGoodsStockCount").increment(seckillId, -1);
        if (count < 0) {
            //库存不足，标识排队状态为没有库存
            redisTemplate.boundHashOps("user_order_info_" + userId).put(seckillId, QueueTag.NO_STOCK);
            throw new RuntimeException("你来晚了一步，商品已抢购一空!");
        }
        //1、从Redis中查询商品信息
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);


        try {
            //开启redis事务
            transRedisTemplate.multi();

            //3、扣减库存,更新商品redis中的库存数量
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            transRedisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
            //4、商品被抢完了，把商品数据从redis同步回mysql
            //高并发场景中，good.StockCount可能不准，此时seckillGoodsStockCount一定是准确的,所以以seckillGoodsCount为标准
            if(count == 0){
                seckillGoods.setStockCount(0);
                //把数据更新回数据库
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //秒杀商品列表中移除当前商品信息
                transRedisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            }
            //5、下单-保存订单到redis
            //在支付之前，保存订单到redis
            long orderId = idWorker.nextId();
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(orderId);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            seckillOrder.setUserId(userId);//设置用户ID
            seckillOrder.setStatus("0");//状态
            //把订单保存redis中,key=用户id,value=TbSeckillOrder
            transRedisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

            //大家不要忘了，下单成功后，要标识排队状态为：已下单
            transRedisTemplate.boundHashOps("user_order_info_" + userId).put(seckillId, QueueTag.CREATE_ORDER);

            System.out.println("用户:" + userId + ",抢购商品id:" + seckillId + "，成功！");
            //int i = 1 / 0;
            //提交事务
            transRedisTemplate.exec();
            //发送MQ延时消息

        } catch (Exception e) {
            e.printStackTrace();
            //取消redis事务
            transRedisTemplate.discard();
            //还原库存
            redisTemplate.boundHashOps("seckillGoodsStockCount").increment(seckillId, 1);
            //记录排队标识为：下单失败
            redisTemplate.boundHashOps("user_order_info_" + userId).put(seckillId, QueueTag.SECKILL_FAIL);
            //如果是最后一个商品下单失败，要回滚mysql表操作
            if(count == 0){
                //把异常抛出去，让spring接管mysql事务
                throw new RuntimeException(e);
            }
        }
    }
}
