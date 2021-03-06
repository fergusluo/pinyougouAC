package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbSpecificationOption;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper optionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize,TbTypeTemplate typeTemplate) {
		PageResult<TbTypeTemplate> result = new PageResult<TbTypeTemplate>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						//如果字段不为空
			if (typeTemplate.getName()!=null && typeTemplate.getName().length()>0) {
				criteria.andLike("name", "%" + typeTemplate.getName() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0) {
				criteria.andLike("specIds", "%" + typeTemplate.getSpecIds() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0) {
				criteria.andLike("brandIds", "%" + typeTemplate.getBrandIds() + "%");
			}
			//如果字段不为空
			if (typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0) {
				criteria.andLike("customAttributeItems", "%" + typeTemplate.getCustomAttributeItems() + "%");
			}
	
		}

        //查询数据
        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(list);
        result.setPages(info.getPages());


        //更新缓存
		this.saveToRedis();

		return result;
	}

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 把品牌与规格列表放入redis
	 */
	private void saveToRedis(){
		List<TbTypeTemplate> all = this.findAll();
		for (TbTypeTemplate typeTemplate : all) {
			//把品牌json串转换为List<Map>对象
			List<Map> brandIds = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
			//Map<模板id,List<Map>
			redisTemplate.boundHashOps("brandIds").put(typeTemplate.getId(), brandIds);

			//把品牌json串转换为List<Map>对象
			List<Map> specList = this.findSpecList(typeTemplate.getId());
			//Map<模板id,List<Map>
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
		}
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insertSelective(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate getById(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量伪删除
	 */
	@Override
	public void delete(Long[] ids) {
		TbTypeTemplate tbTypeTemplate = new TbTypeTemplate();
		//设置删除状态为删除
		tbTypeTemplate.setIsDelete("1");
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据（伪删除，设置状态）
        typeTemplateMapper.updateByExampleSelective(tbTypeTemplate,example);

	}

    @Override
    public List<Map> findSpecList(Long id) {
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		if(typeTemplate != null){
			//先获取规格信息列表[{id:1,text:规格名称}]
			List<Map> specIds = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

			for (Map map : specIds) {
				//查询选项列表
				TbSpecificationOption where = new TbSpecificationOption();
				where.setSpecId(new Long(map.get("id").toString()));
				List<TbSpecificationOption> options = optionMapper.select(where);
				//添加选项信息:[{id:1,text:规格名称,options:[选项1，2，3]}]
				map.put("options", options);
			}
			return specIds;
		}
		return null;
    }

	@Override
	public void updateStatus(Long[] ids, String status) {
		//更新的对象
		TbTypeTemplate record = new TbTypeTemplate();
		//设置状态
		record.setStatus(status);
		//组装条件
		Example example = new Example(TbTypeTemplate.class);
		Example.Criteria criteria = example.createCriteria();
		//数组转换list
		List longs = Arrays.asList(ids);
		criteria.andIn("id", longs);
		typeTemplateMapper.updateByExampleSelective(record, example);
	}


}
