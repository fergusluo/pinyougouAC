package com.pinyougou.test;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.EsItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  ES数据导入工具
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.test
 * @date 2019-5-31
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class EsUtils {
    @Autowired
    private TbItemMapper itemMapper;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ItemDao itemDao;

    @Test
    public void testImportData(){
        TbItem where = new TbItem();
        //只导入启用的商品
        where.setStatus("1");
        List<TbItem> itemList = itemMapper.select(where);
        System.out.println("总共查询到 " + itemList.size() + " 条数据！");
        //声明es对象列表
        List<EsItem> esItemList = new ArrayList<>();
        EsItem esItem = null;
        //组装数据
        System.out.println("开始组装数据...");
        for (TbItem item : itemList) {
            esItem = new EsItem();
            //get...set.....
            //copyProperties(数据源,目标)-把两个对象相同属性名与类型相同的值复制一份
            BeanUtils.copyProperties(item, esItem);
            //把价格转换
            esItem.setPrice(item.getPrice().doubleValue());

            //把嵌套域数据转换
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            esItem.setSpec(specMap);

            //组装数据
            esItemList.add(esItem);
        }
        System.out.println("组装数据完毕，开始导入索引库...");
        //导入索引库
        itemDao.saveAll(esItemList);
    }
}
