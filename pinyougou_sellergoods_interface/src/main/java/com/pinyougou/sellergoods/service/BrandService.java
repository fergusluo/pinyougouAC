package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface BrandService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbBrand> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbBrand> findPage(int pageNum, int pageSize,TbBrand brand);
	
	
	/**
	 * 增加
	*/
	public void add(TbBrand brand);
	
	
	/**
	 * 修改
	 */
	public void update(TbBrand brand);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbBrand getById(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);


	/**
	 * 更新品牌状态，brand表有更改请咨询罗强
	 * @param ids
	 * @param status
	 */
	public void updateBrandStatus(Long[] ids, String status);
}
