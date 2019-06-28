package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbSpecification;

import com.pinyougou.pojogroup.Specification;
import entity.PageResult;
/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface SpecificationService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSpecification> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbSpecification> findPage(int pageNum, int pageSize,TbSpecification specification);
	
	
	/**
	 * 增加
	*/
	public void add(Specification specification);
	
	
	/**
	 * 修改
	 */
	public void update(Specification specification);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Specification getById(Long id);
	
	
	/**
	 * 批量删除,注意数据库表tb_specification，pojo有更改请咨询罗强
	 * @param ids
	 */
	public void delete(Long [] ids);


	/**
	 * 修改规格审核状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long[] ids, String status);
}
