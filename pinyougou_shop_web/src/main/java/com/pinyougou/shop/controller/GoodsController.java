package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.shop.mq.MessageSender;
import entity.EsItem;
import entity.MessageInfo;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	@Autowired
	private MessageSender messageSender;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbGoods goods){
		//商家只能查询自己的商品
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		return goodsService.findPage(pageNo, pageSize,goods);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			//设置商品商家信息
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			goods.getGoods().setSellerId(sellerId);

			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			//先把将要更新的商品信息查询出来
			Goods beUpdate = this.getById(goods.getGoods().getId());
			//商家只能修改自己的商品
			if (!sellerId.equals(beUpdate.getGoods().getSellerId())) {
				return new Result(false, "请注意你的言行，这是一个非法操作！");
			}
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/getById")
	public Goods getById(Long id){
		return goodsService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids) {
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 跟据id列表，上架商品
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/onsale")
	public Result onsale(Long[] ids,String status){
		try {
			goodsService.updownStatus(ids, status);
			//如果是上架成功
			if("1".equals(status)){
				//先查询SKU列表
				List<TbItem> items = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
				if(items != null && items.size() > 0){
					//es数据实体列表
					List<EsItem> esItemList = new ArrayList<>();
					EsItem esItem = null;
					for (TbItem item : items) {
						esItem = new EsItem();
						//使用spring的BeanUtils深克隆对象
						//相当于把TbItem所有属性名与数据类型相同的属性值设置到EsItem中
						BeanUtils.copyProperties(item,esItem);
						//注意，这里价格的类型不一样需要单独设置
						esItem.setPrice(item.getPrice().doubleValue());
						//嵌套域-规格数据绑定
						Map specMap = JSON.parseObject(item.getSpec(), Map.class);
						esItem.setSpec(specMap);
						//组装es实体列表
						esItemList.add(esItem);
					}
					//导入索引库
					//searchService.importList(esItemList);
					//发送MQ消息
					MessageInfo info = new MessageInfo(
							MessageInfo.METHOD_ADD,  //指定增加索引标识
							esItemList,  //消息内容
							"topic-goods-shizhan",
							"tag-goods-onsale",
							"key-goods-onsale"
					);
					messageSender.sendMessage(info);

					//2.生成商品静态化
            /*for (Long goodsId : ids) {
               itemPageService.genItemHtml(goodsId);
            }*/
				}else{
					System.out.println("没有找到SKU数据");
				}
			}
			return new Result(true,"上架成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true,"上架失败!");
		}
	}

	@RequestMapping("/offsale")
	public Result offsale(Long [] ids,String status){
		try {
			goodsService.updownStatus(ids,status);
			//删除索引库
			//searchService.deleteByGoodsId(ids);
			//发送MQ消息
			MessageInfo info = new MessageInfo(
					MessageInfo.METHOD_DELETE,  //指定删除索引标识
					ids, //内容
					"topic-goods-shizhan",
					"tag-goods-offsale",
					"key-goods-offsale"
			);
			messageSender.sendMessage(info);
			return new Result(true, "下架成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "下架失败");
		}
	}
}
