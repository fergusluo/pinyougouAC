package com.pinyougou.es.dao;

import entity.EsItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品信息ES操作对象
 * @author Steven
 * @version 1.0
 * @description com.itheima.es.dao
 * @date 2019-5-31
 */
public interface ItemDao extends ElasticsearchRepository<EsItem,Long> {
    //ElasticsearchRepository删除语法：deleteBy+域名+匹配方式
    void deleteByGoodsIdIn(Long[] goodsIds);
}
