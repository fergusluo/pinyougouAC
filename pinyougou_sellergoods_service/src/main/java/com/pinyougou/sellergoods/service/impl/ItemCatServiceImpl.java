package com.pinyougou.sellergoods.service.impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pinyougou.pojo.TbBrand;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbItemCat itemCat) {
		PageResult<TbItemCat> result = new PageResult<TbItemCat>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						//如果字段不为空
			if (itemCat.getName()!=null && itemCat.getName().length()>0) {
				criteria.andLike("name", "%" + itemCat.getName() + "%");
			}
	
		}

        //查询数据
        List<TbItemCat> list = itemCatMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(list);
        result.setPages(info.getPages());
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insertSelective(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKeySelective(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat getById(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		TbItemCat record = new TbItemCat();
		//设置状态
		record.setIsDelete("1");
		//数组转list
		List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据(伪删除更新是否删除)
        itemCatMapper.updateByExampleSelective(record,example);
	}

	/**
	 * 跟据父节点递归查询所有子节点
	 * @param list 记录所有要删除的节点id
	 * @param id 当前的父母节点
	 */
	private void getAllIds(List<Long> list,Long id){
		//先把当前节点记录起来
		list.add(id);
		//查询所有当前节点的子节点
		List<TbItemCat> itemCatList = this.findByParentId(id);
		if(itemCatList != null && itemCatList.size() > 0){
			for (TbItemCat itemCat : itemCatList) {
				getAllIds(list,itemCat.getId());
			}
		}
	}

	@Autowired
	private RedisTemplate redisTemplate;

    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCat where = new TbItemCat();
		where.setParentId(parentId);
		List<TbItemCat> itemCatList = itemCatMapper.select(where);

		//把所有商品分类查询出来
		List<TbItemCat> all = this.findAll();
		//把分类列表放入redis
		for (TbItemCat itemCat : all) {
			//Map<分类名称,模板id>
			redisTemplate.boundHashOps("itemCats").put(itemCat.getName(), itemCat.getTypeId());
		}
		return itemCatList;
    }

	/**
	 * 表有更改请咨询罗强
	 * @param ids
	 * @param status
	 */
    @Override
    public void updateStatus(Long[] ids, String status) {
		//更新的对象
		TbItemCat record = new TbItemCat();
		//设置状态
		record.setStatus(status);
		//组装条件
		Example example = new Example(TbItemCat.class);
		Example.Criteria criteria = example.createCriteria();
		//数组转换list
		List longs = Arrays.asList(ids);
		criteria.andIn("id", longs);
		itemCatMapper.updateByExampleSelective(record, example);
    }

}
