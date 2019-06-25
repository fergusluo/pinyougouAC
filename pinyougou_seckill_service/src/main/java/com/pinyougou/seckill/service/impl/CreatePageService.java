package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀商品静态页生成
 *
 * @author Steven
 * @description com.pinyougou.seckill.service.impl
 */
@Component
public class CreatePageService {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${PAGE_DIR}")
    private String PAGE_DIR;

    /*****
     * 秒杀商品ID  此处建议使用多线程生成文件
     * @param seckillGoodsId
     * @return
     */
    public boolean buildHtml(Long seckillGoodsId) {
        try {
            //通过Freemarker生成Html
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //构建一个模板对象
            Template template = configuration.getTemplate("seckill-item.ftl");
            //数据模型封装
            Map<String, Object> dataMap = new HashMap<String, Object>();
            //查询秒杀商品信息
            TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillGoodsId);
            dataMap.put("seckillGoods", seckillGoods);
            //准备输出   http://xxx/goodsid.html
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PAGE_DIR + seckillGoodsId + ".html"), "UTF-8"));
            //输出模板
            template.process(dataMap, writer);
            //资源回收
            writer.close();
            System.out.println("生成了商品详情页：" + seckillGoodsId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
