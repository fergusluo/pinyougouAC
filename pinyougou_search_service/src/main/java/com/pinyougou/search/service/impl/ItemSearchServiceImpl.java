package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.search.service.ItemSearchService;
import entity.EsItem;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.Field;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2019-5-31
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map search(Map searchMap) {
        //1、跟据条件查询商品列表
        Map map = searchList(searchMap);
        //2、跟据查询条件，分组查询商品分类列表
        searchCategoryList(searchMap,map);
        //3、查询品牌与规格列表
        //先检测用户有没有传入分类
        String category = searchMap.get("category") == null ? "" : searchMap.get("category").toString();
        if (category.trim().length() < 1) {
            List<String> categoryList = (List<String>) map.get("categoryList");
            if (categoryList != null && categoryList.size() > 0) {
                category = categoryList.get(0);
            }
        }
        searchBrandAndSpecList(category, map);
        return map;
    }

    @Autowired
    private ItemDao itemDao;
    @Override
    public void importList(List list) {
        itemDao.saveAll(list);
    }

    @Override
    public void deleteByGoodsId(Long[] goodsIds) {
        itemDao.deleteByGoodsIdIn(goodsIds);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 3、查询品牌与规格列表
     * @param category 分类名称
     * @param map 结果集
     */
    private void searchBrandAndSpecList(String category, Map map) {
        //过分类名称从tb_item_cat中，查询模板id，缓存获取
        Long typeId = (Long) redisTemplate.boundHashOps("itemCats").get(category);

        //缓存中,通过模板id查询品牌与规格列表-tb_type_template
        if(typeId != null){
            //查询品牌列表
            List<Map> brandIds = (List<Map>) redisTemplate.boundHashOps("brandIds").get(typeId);
            map.put("brandIds", brandIds);
            //查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
    }

    /**
     * 2、跟据查询条件，分组查询商品分类列表
     * @param searchMap 查询条件
     * @param map 结果集
     */
    private void searchCategoryList(Map searchMap, Map map) {
        //1.复制之前的NativeSearchQueryBuilder组装关键字查询条件的代码(不需要高亮查询部分)
        //1.1、创建查询条件构建器-builder = new NativeSearchQueryBuilder()
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //1.2、先默认搜索所有商品-builder.withQuery(QueryBuilders.matchAllQuery())添加一个搜索条件
        builder.withQuery(QueryBuilders.matchAllQuery());
        //1.3、组装查询条件
        String keyword = searchMap.get("keyword") == null ? "" : searchMap.get("keyword").toString();
        if(keyword.trim().length() > 0){
            //1.3.1关键字搜索-builder.withQuery(QueryBuilders.matchQuery(域名，内容))
            builder.withQuery(QueryBuilders.matchQuery("keyword", keyword));
        }
        //2.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_category").field("category");
        //3.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);

        //3.1、获取NativeSearchQuery搜索条件对象-builder.build()
        NativeSearchQuery query = builder.build();
        //4.分组查询查询数据-aggregation = elasticsearchTemplate.query(builder.build(), new ResultsExtractor<Aggregations>(){})
        Aggregations aggregations = elasticsearchTemplate.query(query, new ResultsExtractor<Aggregations>() {
            @Override
            //4.1实现extract(查询结果)方法
            public Aggregations extract(SearchResponse response) {
                //4.2返回方法需要参数-response.getAggregations()
                return response.getAggregations();
            }
        });
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms= aggregations.get("group_category");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中
            categoryList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("categoryList", categoryList);
    }

    /**
     * 1、跟据条件查询商品列表
     * @param searchMap 查询条件
     * @return 结果集
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //1、创建查询条件构建器-builder = new NativeSearchQueryBuilder()
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //2、先默认搜索所有商品-builder.withQuery(QueryBuilders.matchAllQuery())添加一个搜索条件
        builder.withQuery(QueryBuilders.matchAllQuery());

        //h1.配置高亮查询信息-hField = new HighlightBuilder.Field()
        //h1.1:设置高亮域名-在构造函数中设置
        HighlightBuilder.Field hField = new HighlightBuilder.Field("title");
        //h1.2：设置高亮前缀-hField.preTags
        hField.preTags("<em style=\"color:red;\">");
        //h1.3：设置高亮后缀-hField.postTags
        hField.postTags("</em>");
        //h1.4：设置碎片大小-hField.fragmentSize
        hField.fragmentSize(100);
        //h1.5：追加高亮查询信息-builder.withHighlightFields()
        builder.withHighlightFields(hField);


        //3、组装查询条件
        String keyword = searchMap.get("keyword") == null ? "" : searchMap.get("keyword").toString();
        if(keyword.trim().length() > 0){
            //3.1关键字搜索-builder.withQuery(QueryBuilders.matchQuery(域名，内容))
            //builder.withQuery(QueryBuilders.matchQuery("keyword", keyword));
            //h2.不能使用拷贝域做高亮搜索-builder.withQuery(QueryBuilders.multiMatchQuery(内容,域名1，域名2...))
            builder.withQuery(QueryBuilders.multiMatchQuery(keyword,"title","category","brand","seller"));
        }
        //多个过滤条件查询，需要使用BoolQueryBuilder对象来组装
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //3.2 分类查询-前端传入："category": "手机"
        String category = searchMap.get("category") == null ? "" : searchMap.get("category").toString();
        if(category.trim().length() > 0){
            //追加过滤条件,must必须
            //注意商品分类，不应该分词
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("category", category));
        }
        //3.3 品牌查询-前端传入： "brand": "三星"
        String brand = searchMap.get("brand") == null ? "" : searchMap.get("brand").toString();
        if(brand.trim().length() > 0){
            //追加过滤条件,must必须
            //注意商品品牌，不应该分词
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("brand", brand));
        }

        //3.4 规格查询-前端传入： "spec": { "网络": "移动4G", "机身内存": "128G" }
        String spec = searchMap.get("spec") == null ? "" : searchMap.get("spec").toString();
        if(spec.trim().length() > 0){
            //把规格json串转换为map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            //追加多个条件
            for (String key : specMap.keySet()) {
                //嵌套域的name="spec.域名.keyword"
                String name = "spec." + key + ".keyword";
                //QueryBuilders.nestedQuery(nested根节点，wildcardQuery(),ScoreMode.Max(最大得分排序))
                boolQueryBuilder.must(QueryBuilders.nestedQuery("spec", QueryBuilders.wildcardQuery(name, specMap.get(key)), ScoreMode.Max));
            }
        }
        //3.5 规格查询-前端传入： "price": "500-1000"|"3000-*"
        String price = searchMap.get("price") == null ? "" : searchMap.get("price").toString();
        if(price.trim().length() > 0){
            //解析数据：[0,500]
            String[] split = price.split("-");
            //范围搜索条件，构建器
            RangeQueryBuilder priceRange = QueryBuilders.rangeQuery("price");
            //期望  0 <= price <= 500，有特殊情况3000-*
            priceRange.gte(split[0]);
            //排除3000以上条件
            if (!"*".equals(split[1])) {
                priceRange.lte(split[1]);
            }
            //追加过滤条件
            boolQueryBuilder.must(priceRange);
        }

        //追加过滤条件
        builder.withFilter(boolQueryBuilder);

        //4、获取NativeSearchQuery搜索条件对象-builder.build()
        NativeSearchQuery query = builder.build();
        //4.1 分页查询
        //当前页，默认第一页
        Integer pageNo = searchMap.get("pageNo") == null ? 1 : new Integer(searchMap.get("pageNo").toString());
        //查询的条数，默认20条
        Integer pageSize = searchMap.get("pageSize") == null ? 20 : new Integer(searchMap.get("pageSize").toString());
        //设置分页参数
        //PageRequest.of(当前页，注意页数是从0开始的, 查询的条数)
        query.setPageable(PageRequest.of(pageNo - 1, pageSize));
        //4.2 排序查询
        //排序的域名-price|新品
        String sortField = searchMap.get("sortField") == null ? "" : searchMap.get("sortField").toString();
        //排序方式:asc|desc
        String sort = searchMap.get("sort") == null ? "" : searchMap.get("sort").toString();
        if(sortField.trim().length() > 0 && sort.trim().length() > 0){
            Sort orderby = new Sort("asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            query.addSort(orderby);
        }

        //5.查询数据-esTemplate.queryForPage(条件对象,搜索结果对象)
        //AggregatedPage<EsItem> page = elasticsearchTemplate.queryForPage(query, EsItem.class);

        //h3.高亮数据读取-AggregatedPage<EsItem> page = elasticsearchTemplate.queryForPage(query, EsItem.class, new SearchResultMapper(){})
        AggregatedPage<EsItem> page = elasticsearchTemplate.queryForPage(query, EsItem.class, new SearchResultMapper() {
            @Override
            //h3.1实现mapResults(查询到的结果,数据列表的类型,分页选项)方法
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //h3.2 先定义一组查询结果列表-List<T> list = new ArrayList<T>()
                List<T> list = new ArrayList<T>();

                //h3.3 遍历查询到的所有高亮数据-response.getHits().for
                for (SearchHit hit : response.getHits()) {
                    //h3.3.1 先获取当次结果的原始数据(无高亮)-hit.getSourceAsString()
                    String sourceAsString = hit.getSourceAsString();
                    //h3.3.2 把json串转换为EsItem对象-esItem = JSON.parseObject()
                    EsItem esItem = JSON.parseObject(sourceAsString, EsItem.class);
                    //h3.3.3 获取title域的高亮数据-titleHighlight = hit.getHighlightFields().get("title")
                    HighlightField titleHighlight = hit.getHighlightFields().get("title");
                    //h3.3.4 如果高亮数据不为空-读取高亮数据
                    if(titleHighlight != null){
                        //h3.3.4.1 定义一个StringBuffer用于存储高亮碎片-buffer = new StringBuffer()
                        StringBuffer buffer = new StringBuffer();
                        //h3.3.4.2 循环组装高亮碎片数据-titleHighlight.getFragments().for(追加数据)
                        for (Text fragment : titleHighlight.getFragments()) {
                            buffer.append(fragment);
                        }
                        //h3.3.4.3 将非高亮数据替换成高亮数据-esItem.setTitle()
                        esItem.setTitle(buffer.toString());
                    }
                    //h3.3.5 将替换了高亮数据的对象封装到List中-list.add((T) esItem)
                    list.add((T) esItem);
                }
                //h3.4 返回当前方法所需要参数-new AggregatedPageImpl<T>(数据列表，分页选项,总记录数)
                //h3.4 参考new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits())
                return new AggregatedPageImpl<T>(list, pageable, response.getHits().getTotalHits());
            }
        });
        //6、包装结果并返回
        map.put("rows", page.getContent());
        //返回分页参数
        map.put("total", page.getTotalElements());  //总记录数
        map.put("totalPages", page.getTotalPages());  //总页数
        return map;
    }
}
