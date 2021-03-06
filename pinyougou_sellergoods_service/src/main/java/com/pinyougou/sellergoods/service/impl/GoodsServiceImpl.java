package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbGoods goods) {
		PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						//如果字段不为空
			if (goods.getSellerId()!=null && goods.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
			}
			//如果字段不为空
			if (goods.getGoodsName()!=null && goods.getGoodsName().length()>0) {
				criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
			}
			//如果字段不为空
			if (goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0) {
				criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
			}
			//如果字段不为空
			if (goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0) {
				criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
			}
			//如果字段不为空
			if (goods.getCaption()!=null && goods.getCaption().length()>0) {
				criteria.andLike("caption", "%" + goods.getCaption() + "%");
			}
			//如果字段不为空
			if (goods.getSmallPic()!=null && goods.getSmallPic().length()>0) {
				criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
			}
			//如果字段不为空
			if (goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0) {
				criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
			}
			//如果字段不为空
			/*if (goods.getIsDelete()!=null && goods.getIsDelete().length()>0) {
				criteria.andLike("isDelete", "%" + goods.getIsDelete() + "%");
			}*/
			//只查询未删除的商品
			criteria.andIsNull("isDelete");
	
		}

        //查询数据
        List<TbGoods> list = goodsMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setPages(info.getPages());
		
		return result;
	}

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//保存商品基本信息
		//新增商品是未审核状态
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insertSelective(goods.getGoods());

		//int i = 1 / 0;
		//保存商品扩展信息
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insertSelective(goods.getGoodsDesc());
		//保存商品sku列表
		saveItemList(goods);
	}

	private void saveItemList(Goods goods) {
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//保存商品sku列表
			for (TbItem item : goods.getItemList()) {
				//商品标题
				String title = goods.getGoods().getGoodsName();
				Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				item.setTitle(title);
				//设置商品sku详细信息
				setItemValus(goods, item);

				//保存商品sku信息
				itemMapper.insertSelective(item);
			}
		}else{
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods,item);
			itemMapper.insertSelective(item);
		}
	}

	/**
	 * 设置商品详细信息
	 * @param goods
	 * @param item
	 */
	private void setItemValus(Goods goods, TbItem item) {
		//卖点
		item.setSellPoint(goods.getGoods().getCaption());
		//默认读取第一张图片
		List<Map> imgList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imgList != null && imgList.size() > 0) {
			item.setImage(imgList.get(0).get("url").toString());
		}
		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
		item.setCategory(tbItemCat.getName());
		//创建日期
		item.setCreateTime(new Date());
		//更新日期
		item.setUpdateTime(item.getCreateTime());

		//所属SPU-id
		item.setGoodsId(goods.getGoods().getId());
		//所属商家
		item.setSellerId(goods.getGoods().getSellerId());
		TbSeller seller = sellerMapper.selectByPrimaryKey(item.getSellerId());
		item.setSeller(seller.getNickName());

		//品牌信息
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新商品基本信息
		goods.getGoods().setAuditStatus("0"); //修改商品后要重新审核
		goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
		//更新商品扩展信息
		goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

		//更新商品sku列表
		//先删除原来的sku
		TbItem where = new TbItem();
		where.setGoodsId(goods.getGoods().getId());
		itemMapper.delete(where);
		//重新添加列表
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods getById(Long id){
		Goods goods = new Goods();
		//查询商品基本信息
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//查询商品扩展信息
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//查询商品sku列表
		TbItem where = new TbItem();
		where.setGoodsId(id);
		List<TbItem> itemList = itemMapper.select(where);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        //goodsMapper.deleteByExample(example);

		//只做逻辑删除
		TbGoods record = new TbGoods();
		record.setIsDelete("1");
		goodsMapper.updateByExampleSelective(record, example);
	}

    @Override
    public void updateStatus(Long[] ids, String status) {
		//修改的结果
		TbGoods record = new TbGoods();
		record.setAuditStatus(status);
		//修改的条件
		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		List longs = Arrays.asList(ids);
		criteria.andIn("id", longs);
		goodsMapper.updateByExampleSelective(record,example);
    }

	/**
	 * 根据goodsId和商品上下架状态查询item表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds, String status) {
		//组装查询条件
		Example example = new Example(TbItem.class);
		Example.Criteria criteria = example.createCriteria();
		List longs = Arrays.asList(goodsIds);
		criteria.andIn("goodsId", longs);
		criteria.andEqualTo("updownStatus", status);

		List<TbItem> itemList = itemMapper.selectByExample(example);
		return itemList;
    }

	/**根据id列表和传入的上下架状态，上下架商品
	 * @param ids
	 * @param status
	 */
	@Override
	public void updownStatus(Long[] ids, String status) {
		//goods表更新
		TbGoods record = new TbGoods();
		record.setIsMarketable(status);
		//更新条件组装
		Example example1 = new Example(TbGoods.class);
		Example.Criteria criteria1 = example1.createCriteria();
		List list1 = Arrays.asList(ids);
		criteria1.andIn("id", list1);
		//执行更新
		goodsMapper.updateByExampleSelective(record,example1);

		//item表更新
		TbItem item=new TbItem();
		item.setUpdownStatus(status);
		//更新条件组装
		Example example2 = new Example(TbItem.class);
		Example.Criteria criteria2 = example2.createCriteria();
		List list2 = Arrays.asList(ids);
		criteria2.andIn("goodsId", list2);
		//执行更新
		itemMapper.updateByExampleSelective(item, example2);
	}

	///**跟据id列表，下架商品
	// * @param ids
	// * @param status
	// */
	//@Override
	//public void offsale(Long[] ids,String status) {
	//	//goods表更新
	//	TbGoods record = new TbGoods();
	//	record.setIsMarketable(status);
	//	//更新条件组装
	//	Example example1 = new Example(TbGoods.class);
	//	Example.Criteria criteria1 = example1.createCriteria();
	//	List list1 = Arrays.asList(ids);
	//	criteria1.andIn("id", list1);
	//	//执行更新
	//	goodsMapper.updateByExampleSelective(record, example1);
	//
	//	//item表更新
	//	TbItem item=new TbItem();
	//	item.setUpdownStatus(status);
	//	//更新条件组装
	//	Example example2 = new Example(TbItem.class);
	//	Example.Criteria criteria2 = example2.createCriteria();
	//	List list2 = Arrays.asList(ids);
	//	criteria2.andIn("id", list2);
	//	//执行更新
	//	itemMapper.updateByExampleSelective(item, example2);
	//}
}
