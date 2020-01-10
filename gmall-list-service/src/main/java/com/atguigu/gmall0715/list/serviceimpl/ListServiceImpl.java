package com.atguigu.gmall0715.list.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.SkuLsInfo;
import com.atguigu.gmall0715.bean.SkuLsParams;
import com.atguigu.gmall0715.bean.SkuLsResult;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    //调用操作es的客户端
    @Autowired
    private JestClient jestClient;
    //操作reids的工具类
    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX = "gmall";
    public static final String ES_TYPE = "SkuInfo";

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        //1.定义好动作 PUT /gmall/SkuInfo0715/37 {"id":"xxx",...}
        Index build = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        //2.执行
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {// 三种参数 keyword、catalog3Id、valueId
        //1.定义好DSL语句
        String query = makeQueryStringForSearch(skuLsParams);
        //2.准备好执行的动作
        Search build = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //3.获取结果集
        SkuLsResult skuLsResult = makeResultForSearch(searchResult, skuLsParams);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        //记录用户访问商品的次数
        //获取Jedis
        Jedis jedis = redisUtil.getJedis();
        //zSet
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if (hotScore % 10 == 0) {
            //更新es
            updateHotScore(skuId, Math.round(hotScore));
        }

    }

    //更新es的hotScore(热度)
    private void updateHotScore(String skuId, long hotScore) {
        //1.定义dsl语句POST gmall/SkuInfo/38/_update {"doc": {"hotScore":20}}
        String upd = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":" + hotScore + "\n" +
                "  }\n" +
                "}";
        //2.准备执行的动作
        Update build = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        //3.执行
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 制作返回结果集
     *
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();
        //skuLsInfoList： 保存商品的
        ArrayList<SkuLsInfo> skuLsInfos = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if (hits != null && hits.size() > 0) {
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;
                //如果走全文检索skuName不是高亮，的到高亮
                if (hit.highlight != null && hit.highlight.size() > 0) {
                    List<String> skuName = hit.highlight.get("skuName");
                    String skuNameHI = skuName.get(0);
                    skuLsInfo.setSkuName(skuNameHI);
                }
                skuLsInfos.add(skuLsInfo);
            }
        }
        //total: 总条数
        skuLsResult.setTotal(searchResult.getTotal());
        //totalPages: 总页数
        //long totalPages = skuLsResult.getTotal() % skuLsParams.getPageSize() == 0 ? skuLsResult.getTotal() / skuLsParams.getPageSize() : skuLsResult.getTotal() / skuLsParams.getPageSize() + 1;
        long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);
        //平台属性值valueId集合 显示平台属性，平台属性值
        ArrayList<String> list = new ArrayList<>();
        TermsAggregation groupby_attr = searchResult.getAggregations().getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        if (buckets != null && buckets.size() > 0) {
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                list.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(list);

        //将数据全部封装
        skuLsResult.setSkuLsInfoList(skuLsInfos);
        return skuLsResult;
    }

    /**
     * 根据用户输入的检索条件生成DSL语句
     *  GET index/type/_search {"query":{"bool":{"filter":{"term":""},"must":[{"match":""},{..}]}},"form":1,size:2,}
     *
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //定义查询器 类似于: { }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //{query -> bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断是否按照三级分类id过滤 {bool -> filter}
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            //建立单个条件 {filter -> "term":{"fn":"fv"}}
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //判断是否按照平台属性值id过滤 {bool -> filter}
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            //多个条件条件 -> 循环  {filter -> "term":[{"fn":"fv"}, ...]}
            for (String valueId : skuLsParams.getValueId()) {
                //判断平台属性
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //判断是否有skuName -> keyword 分词查询 {bool -> must}
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            //分词查询条件 {must -> match }
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            //高亮设置 {highhight}
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        //根据热点排序 {sort}
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        //分页 {from & size}
        //从第几条开始查
        int from = skuLsParams.getPageSize() * (skuLsParams.getPageNo() - 1);
        searchSourceBuilder.from(from);
        //默认每页显示20条数据
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //聚合 {aggs -> "groupbyxxx" 名称 -> terms (过滤条件)}
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        //{query->bool->filter->term}
        searchSourceBuilder.query(boolQueryBuilder);
        String query = searchSourceBuilder.toString();
        System.out.println("query: " + query);
        return query;
    }
}
