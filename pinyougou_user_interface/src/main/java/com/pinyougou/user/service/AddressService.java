package com.pinyougou.user.service;
import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbAddress;

import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import entity.PageResult;
/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface AddressService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbAddress> findAll();
	
	
	/**
     * 分页查询列表
     * @return
     */
    public PageResult<TbAddress> findPage(int pageNum, int pageSize, TbAddress address);
	
	
	/**
	 * 增加
	*/
	public void add(TbAddress address);
	
	
	/**
	 * 修改
	 */
	public void update(TbAddress address);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbAddress getById(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 根据用户查询地址
	 * @param userId
	 * @return
	 */
	public List<TbAddress> findListByUserId(String userId);

	/**
	 * 查询省份
	 *
	 * @return
	 */
	public List<TbProvinces> findProvinces();
	/**
	 * 查询市
	 *
	 * @return
	 */
	public List<TbCities> findCities(String parentId);
	/**
	 * 查询地区
	 *
	 * @return
	 */
	public List<TbAreas> findAreas(String parentId);

	/**
	 * 查询详细地址
	 *
	 * @return
	 */
	public Map<String,String> addressMap();
	/**
	 * 单个删除
	 * @param id
	 */
	public int deleteOne(Long id);
	/**
	 * 设为默认
	 * @param id
	 */
	public int setDefault(Long id, String userId);
}
