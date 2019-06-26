package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize, TbBrand brand) {
        PageResult<TbBrand> result = new PageResult<TbBrand>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();

        if (brand != null) {
            //如果字段不为空
            if (brand.getName() != null && brand.getName().length() > 0) {
                criteria.andLike("name", "%" + brand.getName() + "%");
            }
            //如果字段不为空
            if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
                criteria.andLike("firstChar", "%" + brand.getFirstChar() + "%");
            }
            //如果字段不为空
            if (brand.getBrandStatus() != null && brand.getBrandStatus().length() > 0) {
                criteria.andLike("brandStatus", "%" + brand.getBrandStatus() + "%");
            }
            //如果字段不为空
            if (brand.getSellerId() != null && brand.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + brand.getSellerId() + "%");
            }

        }

        criteria.andIn("isDelete", Collections.singletonList(0));
        //查询数据
        List<TbBrand> list = brandMapper.selectByExample(example);
        //返回数据列表
        result.setRows(list);

        //获取总页数
        PageInfo<TbBrand> info = new PageInfo<TbBrand>(list);
        result.setPages(info.getPages());

        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbBrand brand) {

        //构建查询条件
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("name", "%" + brand.getName() + "%");
        criteria.andIn("isDelete", Collections.singletonList(0));
        List<TbBrand> tbBrands = brandMapper.selectByExample(example);
        //当有重名时不添加
        if (tbBrands.size() == 0) {
            brandMapper.insertSelective(brand);
            return;
        }
        int i = 10 / 0;


    }


    /**
     * 修改
     */
    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbBrand getById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        brandMapper.deleteByExample(example);
    }


}
