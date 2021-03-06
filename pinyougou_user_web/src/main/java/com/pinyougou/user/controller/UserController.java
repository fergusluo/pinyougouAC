package com.pinyougou.user.controller;
import java.util.List;

import com.pinyougou.user.service.UserService;
import com.pinyougou.utils.PhoneFormatCheckUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;

import entity.PageResult;
import entity.Result;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	/**
	 * 分页查询数据
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int pageNo,int pageSize,@RequestBody TbUser user){			
		return userService.findPage(pageNo, pageSize,user);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		try {
			//验证验证码
			if (!userService.checkSmsCode(user.getPhone(),code)) {
				return new Result(false, "验证码输入不正确！");
			}

			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
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
	public TbUser getById(Long id){
		return userService.getById(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	@RequestMapping("createSmsCode")
	public Result createSmsCode(String phone){
		try {
			//验证手机号的合法性
			if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
				return new Result(false, "请输入正确的手机号！");
			}
			userService.createSmsCode(phone);
			return new Result(true, "验证码已成功发送！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Result(false, "验证码发送失败！");
	}
	@RequestMapping("/getByUsername")
	public TbUser getByUsername() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userService.getByUsername(username);
	}
}
