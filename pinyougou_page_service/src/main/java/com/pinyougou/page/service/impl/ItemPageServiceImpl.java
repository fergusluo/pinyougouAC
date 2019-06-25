package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2019-6-5
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper descMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${PAGE_DIR}")
    private String PAGE_DIR;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //查询商品信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc goodsDesc = descMapper.selectByPrimaryKey(goodsId);
            //freemarker生成页面
            Configuration cfg = freeMarkerConfigurer.getConfiguration();
            //获取模板
            Template template = cfg.getTemplate("item.ftl");
            //构建数据模型
            Map map = new HashMap();

            //返回商品信息
            map.put("goods", goods);
            map.put("goodsDesc", goodsDesc);

            //商品三级分类
            String itemCat1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("itemCat1Name", itemCat1Name);
            map.put("itemCat2Name", itemCat2Name);
            map.put("itemCat3Name", itemCat3Name);

            //查询商品sku列表
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            //匹配sku列表
            criteria.andEqualTo("goodsId", goodsId);
            //只查询启用的商品
            criteria.andEqualTo("status", "1");
            //设置默认排序，把默认商品放到第一条
            example.setOrderByClause("isDefault desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList", itemList);

            //文件输出目录
            Writer out = new FileWriter(PAGE_DIR + goodsId + ".html");
            //把模模板与数据输出文档
            template.process(map,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
