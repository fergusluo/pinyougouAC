package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbOrder order) {
		PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						//如果字段不为空
			if (order.getPaymentType()!=null && order.getPaymentType().length()>0) {
				criteria.andLike("paymentType", "%" + order.getPaymentType() + "%");
			}
			//如果字段不为空
			if (order.getPostFee()!=null && order.getPostFee().length()>0) {
				criteria.andLike("postFee", "%" + order.getPostFee() + "%");
			}
			//如果字段不为空
			if (order.getStatus()!=null && order.getStatus().length()>0) {
				criteria.andLike("status", "%" + order.getStatus() + "%");
			}
			//如果字段不为空
			if (order.getShippingName()!=null && order.getShippingName().length()>0) {
				criteria.andLike("shippingName", "%" + order.getShippingName() + "%");
			}
			//如果字段不为空
			if (order.getShippingCode()!=null && order.getShippingCode().length()>0) {
				criteria.andLike("shippingCode", "%" + order.getShippingCode() + "%");
			}
			//如果字段不为空
			if (order.getUserId()!=null && order.getUserId().length()>0) {
				criteria.andLike("userId", "%" + order.getUserId() + "%");
			}
			//如果字段不为空
			if (order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0) {
				criteria.andLike("buyerMessage", "%" + order.getBuyerMessage() + "%");
			}
			//如果字段不为空
			if (order.getBuyerNick()!=null && order.getBuyerNick().length()>0) {
				criteria.andLike("buyerNick", "%" + order.getBuyerNick() + "%");
			}
			//如果字段不为空
			if (order.getBuyerRate()!=null && order.getBuyerRate().length()>0) {
				criteria.andLike("buyerRate", "%" + order.getBuyerRate() + "%");
			}
			//如果字段不为空
			if (order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0) {
				criteria.andLike("receiverAreaName", "%" + order.getReceiverAreaName() + "%");
			}
			//如果字段不为空
			if (order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0) {
				criteria.andLike("receiverMobile", "%" + order.getReceiverMobile() + "%");
			}
			//如果字段不为空
			if (order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0) {
				criteria.andLike("receiverZipCode", "%" + order.getReceiverZipCode() + "%");
			}
			//如果字段不为空
			if (order.getReceiver()!=null && order.getReceiver().length()>0) {
				criteria.andLike("receiver", "%" + order.getReceiver() + "%");
			}
			//如果字段不为空
			if (order.getInvoiceType()!=null && order.getInvoiceType().length()>0) {
				criteria.andLike("invoiceType", "%" + order.getInvoiceType() + "%");
			}
			//如果字段不为空
			if (order.getSourceType()!=null && order.getSourceType().length()>0) {
				criteria.andLike("sourceType", "%" + order.getSourceType() + "%");
			}
			//如果字段不为空
			if (order.getSellerId()!=null && order.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
			}
	
		}

        //查询数据
        List<TbOrder> list = orderMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1、从redis中查询出要结果的商品信息(购物车列表)
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		List<String> orderIdList=new ArrayList();//订单ID列表
		double totalMoney = 0.0;//总金额 （元）

		//2、以购物车列表为单位进行拆单流程-一个商家一张订单
		for (Cart cart : cartList) {
			//创建订单对象
			TbOrder beSave = new TbOrder();
			//生成订单号
			long orderId = idWorker.nextId();

			//记录订单号列表
			orderIdList.add(orderId + "");
			beSave.setOrderId(orderId);
			beSave.setPaymentType(order.getPaymentType());  //支付方式
			beSave.setStatus("1");  //未付款状态
			beSave.setCreateTime(new Date());  //创建时间
			beSave.setUpdateTime(beSave.getCreateTime());  //更新时间
			beSave.setUserId(order.getUserId());  //下单人
			beSave.setReceiverAreaName(order.getReceiverAreaName());//收货地址
			beSave.setReceiverMobile(order.getReceiverMobile());//手机号
			beSave.setReceiver(order.getReceiver());//收货人
			beSave.setSourceType(order.getSourceType());  //订单来源
			beSave.setSellerId(cart.getSellerId());  //订单发货的商家
			//实付金额
			double money = 0.0;
			//保存订单商品列表
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				//订单商品id
				orderItem.setId(idWorker.nextId());
				//设置订单号
				orderItem.setOrderId(orderId);
				//统计金额....................
				money += orderItem.getTotalFee().doubleValue();

				orderItemMapper.insertSelective(orderItem);
			}
			//计算支付总金额
			totalMoney += money;
			//实体金额
			beSave.setPayment(new BigDecimal(money));
			//保存订单
			orderMapper.insertSelective(beSave);
		}
		//保存日志，在线支付才记录日志
		if("1".equals(order.getPaymentType())){
			//可以多个订单一起支付
			TbPayLog payLog = new TbPayLog();
			String outTradeNo = idWorker.nextId() + "";//支付订单号
			payLog.setOutTradeNo(outTradeNo);//支付订单号
			payLog.setCreateTime(new Date());//创建时间
			//订单号列表，逗号分隔
			String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			payLog.setOrderList(ids);//订单号列表，逗号分隔
			payLog.setPayType("1");//支付类型
			payLog.setTotalFee((long) (totalMoney * 100));//总金额(分)
			payLog.setTradeState("0");//支付状态,未支付  1已支付
			payLog.setUserId(order.getUserId());//用户ID

			//保存支付日志-未支付
			payLogMapper.insertSelective(payLog);

			//把日志保存到redis中，以用户id为key
			redisTemplate.boundHashOps("payLogs").put(order.getUserId(), payLog);
		}
		//3、清空购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKeySelective(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder getById(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        orderMapper.deleteByExample(example);
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLogs").get(userId);
		return payLog;
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1、修改支付日志，记录流水号
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		if(payLog != null){
			payLog.setPayTime(new Date());  //支付时间
			payLog.setTradeState("1");  //已支付
			payLog.setTransactionId(transaction_id);
			payLogMapper.updateByPrimaryKeySelective(payLog);
			//2、修改关联订单状态为-已付款
			String[] ids = payLog.getOrderList().split(",");
			for (String id : ids) {
				TbOrder beUpdate = new TbOrder();
				beUpdate.setOrderId(new Long(id));
				beUpdate.setStatus("2");
				orderMapper.updateByPrimaryKeySelective(beUpdate);
			}
			//3、清除支付的redis内容
			redisTemplate.boundHashOps("payLogs").delete(payLog.getUserId());
		}

	}
}
