package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.user.service.AddressService;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("address")
public class AddressController {

	@Reference
	private AddressService addressService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbAddress> findAll(){			
		return addressService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbAddress address){			
		return addressService.findPage(pageNo, pageSize,address);
	}
	
	/**
	 * 增加
	 * @param address
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbAddress address){
		try {
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			address.setUserId(userId);
			address.setIsDefault("0");
			addressService.add(address);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param address
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbAddress address){
		try {
			addressService.update(address);
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
	public TbAddress getById(Long id){
		return addressService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			addressService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	@RequestMapping("/findListByLoginUser")
	public List<TbAddress> findListByLoginUser(){
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		List<TbAddress> addressList = addressService.findListByUserId(userId);
		return addressList;
	}
	@RequestMapping("/findProvinces")
	public List<TbProvinces> findProvinces() {
		return addressService.findProvinces();
	}
	@RequestMapping("/findCities")
	public List<TbCities> findCities(String parentId) {
		return addressService.findCities(parentId);
	}
	@RequestMapping("/findAreas")
	public List<TbAreas> findAreas(String parentId) {
		return addressService.findAreas(parentId);
	}
	@RequestMapping("/addressMap")
	public Map<String, String> addressMap() {
		return addressService.addressMap();
	}
	@RequestMapping("/deleteOne")
	public Result deleteOne(Long id) {
		int i = addressService.deleteOne(id);
		Result result=null;
		if (i>0){
		    result=new Result(true,"删除成功！");
		}else {
			result=new Result(false,"删除失败！");
		}
		return result;
	}
	@RequestMapping("/setDefault")
	public Result setDefault(Long id) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		int i = addressService.setDefault(id, userId);
		Result result=null;
		if (i>0){
		    result=new Result(true,"设置成功!");
		}else{
			result=new Result(false,"设置失败!");
		}
		return result;
	}
}
