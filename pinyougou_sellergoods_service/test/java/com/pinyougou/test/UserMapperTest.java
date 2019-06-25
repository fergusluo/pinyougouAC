package com.pinyougou.test;

import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

/**
 * 通用Mapper入门
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.test
 * @date 2019-5-22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testFindAll(){
        List<User> userList = userMapper.select(null);
        for (User user : userList) {
            System.out.println(user);
        }
    }

    @Test
    //不忽略空值
    public void testInsert(){
        User user = new User();
        user.setUsername("小哥哥");
        user.setSex("1");
        userMapper.insert(user);
    }

    @Test
    //忽略空值
    public void testInsertSelective(){
        User user = new User();
        user.setUsername("小姐姐");
        user.setSex("0");
        userMapper.insertSelective(user);
    }

    @Test
    public void testUpdate(){
        User user = new User();
        user.setUsername("小姐姐很可爱");
        user.setId(38);
        userMapper.updateByPrimaryKey(user);
    }

    @Test
    public void testUpdateSelective(){
        User user = new User();
        user.setUsername("小姐姐很可爱");
        user.setId(38);
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Test
    public void testUpdateByExampleSelective(){
        //把生日改成今天
        User record = new User();
        record.setBirthday(new Date());
        //修改条件
        Example example = new Example(User.class);
        //用于构建查询条件
        Example.Criteria criteria = example.createCriteria();
        //(pojo属性名，属性值)
        criteria.andEqualTo("sex", "0");
        //criteria.andLike("username", "%貂%");

        //updateByExampleSelective(修改成结果,修改的条件)
        userMapper.updateByExampleSelective(record,example);
    }

    @Test
    public void testGetById(){
        User user = userMapper.selectByPrimaryKey(33);
        System.out.println(user);
    }

    @Test
    public void testFindOne(){
        User where = new User();
        where.setUsername("Steven");
        User user = userMapper.selectOne(where);
        System.out.println(user);
    }

    @Test
    public void testSelect(){
        User where = new User();
        where.setSex("0");
        List<User> userList = userMapper.select(where);
        for (User user : userList) {
            System.out.println(user);
        }
    }

    @Test
    public void testSelectByExample(){
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sex", "0");

        List<User> userList = userMapper.selectByExample(example);
        for (User user : userList) {
            System.out.println(user);
        }
    }

    @Test
    public void testSelectCount(){
        User where = new User();
        where.setSex("0");
        int count = userMapper.selectCount(where);
        System.out.println("总记录数为：" + count);
    }

    @Test
    public void testDelete(){
        userMapper.deleteByPrimaryKey(31);
    }

    @Test
    public void testDeleteByExample(){

        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIsNull("address");

        userMapper.deleteByExample(example);
    }

    @Test
    public void testFindByPage(){
        //设置分页参数
        PageHelper.startPage(1, 10);
        List<User> userList = userMapper.select(null);

        PageInfo<User> info = new PageInfo<>(userList);
        for (User user : userList) {
            System.out.println(user);
        }
    }
}
