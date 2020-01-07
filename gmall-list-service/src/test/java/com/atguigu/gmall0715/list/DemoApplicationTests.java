package com.atguigu.gmall0715.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DemoApplicationTests {
    //操作ES
    //启动时自动注入es客户端
    @Autowired
    private JestClient jestClient;

    @Test
    public void testES(){
        //执行命令
        //1.定义DSL语句
        try {
            String query = "{\n" +
                    "  \"query\": {\n" +
                    "    \"match\": {\n" +
                    "      \"doubanScore\": \"7.0\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            //2.确定动作准备执行
            Search search = new Search.Builder(query).addIndex("movie_index").addType("movie").build();

            SearchResult searchResult = jestClient.execute(search);
            //3.获取返回结果
            List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);
            for (SearchResult.Hit<Map, Void> hit : hits) {
                Map source = hit.source;
                System.out.println(source.get("name"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void contextLoads() {
    }

}
