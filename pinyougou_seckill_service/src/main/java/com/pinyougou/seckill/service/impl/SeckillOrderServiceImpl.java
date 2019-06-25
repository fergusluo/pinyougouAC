package com.pinyougou.seckill.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.utils.IdWorker;
import entity.QueueTag;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbSeckillOrder seckillOrder) {
		PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						//如果字段不为空
			if (seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0) {
				criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0) {
				criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0) {
				criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0) {
				criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0) {
				criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0) {
				criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
			}
	
		}

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insertSelective(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder getById(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private MultiThreadWork multiThreadWork;

    @Override
    public void submitOrder(Long seckillId, String userId) {
    	//排队前先查询当前商品用户有没有未付款订单
		QueueTag tag = (QueueTag) redisTemplate.boundHashOps("user_order_info_" + userId).get(seckillId);
		//用户正在排队或者已经有未支付订单
		if(tag == QueueTag.IN_LINE || tag == QueueTag.CREATE_ORDER){
			throw new RuntimeException("当前商品您已经有了未付款订单，请先支付！");
		}
		//登记录排队信息,左进右取，以商品id为key，用户id为value
		redisTemplate.boundListOps("seckill_goods_order_queue_" + seckillId).leftPush(userId);
		//记录排队信息,redis中key=用户id：｛商品id:排队标识｝
		redisTemplate.boundHashOps("user_order_info_" + userId).put(seckillId, QueueTag.IN_LINE);

		//调起多线程下单，后续追加.....
		multiThreadWork.createOrder(seckillId);
	}

	@Override
	public QueueTag getQueueStatus(Long seckillId, String userId) {
		QueueTag tag = (QueueTag) redisTemplate.boundHashOps("user_order_info_" + userId).get(seckillId);
		return tag;
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		return seckillOrder;
	}

    @Override
    public void saveOrderFromRedisToDb(String userId, String transactionId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder == null){
			throw new RuntimeException("订单信息不存在！");
		}
		seckillOrder.setTransactionId(transactionId);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//状态
		//保存订单到数据库
		seckillOrderMapper.insertSelective(seckillOrder);
		//清空当前用户在redis中的订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
		//标记排队状态为已支付
		redisTemplate.boundHashOps("user_order_info_" + userId).put(seckillOrder.getSeckillId(), QueueTag.PAY_SUCCESS);
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder != null){
			//1、删除redis中的订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//2、还原库存
			//把商品信息先查询出来
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			//还原库存-列表页读取
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			//还原库存-分布式抢单用的库存
			redisTemplate.boundHashOps("seckillGoodsStockCount").increment(seckillOrder.getSeckillId(), 1);
		}
	}
}
