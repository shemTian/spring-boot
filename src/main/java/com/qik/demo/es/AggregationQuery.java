package com.qik.demo.es;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * AggregationQuery
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 16:38
 **/

public class AggregationQuery {
    public static void main(String[] args) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //年龄0-5 select count(1) from table where age>=0 and age<5
        RangeAggregationBuilder age05 = AggregationBuilders.range("0-5")
                .field("age")
                .addRange(0,5);
        //年龄5-10 select count(1) from table where age>=5 and age<10
        RangeAggregationBuilder age510 = AggregationBuilders.range("5-10")
                .field("age")
                .addRange(0,5);
        //星座散列select galaxy,count(1) from table group by galaxy;
        TermsAggregationBuilder xinzuoGroup = AggregationBuilders.terms("xingzuo_group")
                .field("galaxy");

        // id不为空
        FilterAggregationBuilder filter = AggregationBuilders.filter("id", QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("id", "")));

        GlobalAggregationBuilder agg = AggregationBuilders
                .global("gloable_group")
                .subAggregation(xinzuoGroup)
                .subAggregation(age05)
                .subAggregation(age510)
                .subAggregation(filter);
        searchSourceBuilder.aggregation(agg);
        System.out.println(searchSourceBuilder);
    }
}
