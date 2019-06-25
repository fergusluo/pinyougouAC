package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.manager.mq.MessageSender;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.EsItem;
import entity.MessageInfo;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	/*@Reference
	private ItemSearchService itemSearchService;*/
	/*@Reference
	private ItemPageService pageService*/;

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
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			//删除索引库
			//itemSearchService.deleteByGoodsId(ids);
			//发一条新索引消息
			MessageInfo info = new MessageInfo(
					MessageInfo.METHOD_DELETE,  //删除操作
					ids,  //消息内容
					"topic-goods",
					"tag-goods-delete",
					"key-goods-delete"

			);
			messageSender.sendMessage(info);

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	@RequestMapping("updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids,status);
			//如果是审核通过
			if("1".equals(status)){
				//同步索引库
				//1、跟据SPU-ID列表和状态，查询SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdsAndStatus(ids, status);

				//声明es对象列表
				List<EsItem> esItemList = new ArrayList<>();
				EsItem esItem = null;
				//组装数据
				for (TbItem item : itemList) {
					esItem = new EsItem();
					//get...set.....
					//copyProperties(数据源,目标)-把两个对象相同属性名与类型相同的值复制一份
					BeanUtils.copyProperties(item, esItem);
					//把价格转换
					esItem.setPrice(item.getPrice().doubleValue());

					//把嵌套域数据转换
					Map specMap = JSON.parseObject(item.getSpec(), Map.class);
					esItem.setSpec(specMap);

					//组装数据
					esItemList.add(esItem);
				}
				//2、批量导入数据
				//itemSearchService.importList(esItemList);
				//发一条新索引消息
				MessageInfo info = new MessageInfo(
						MessageInfo.METHOD_ADD,  //新增操作
						esItemList,  //消息内容
						"topic-goods",
						"tag-goods-add",
						"key-goods-add"

				);
				messageSender.sendMessage(info);

				//3、生成商品详情页
				/*for (Long id : ids) {
					pageService.genItemHtml(id);
				}*/

			}
			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}


	/*@RequestMapping("genHtml")
	public boolean genHtml(Long goodsId){
		//生成页面调用
		return pageService.genItemHtml(goodsId);
	}*/
}
