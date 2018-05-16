package com.qik.demo.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;

/**
 * WrapperQuery
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 16:30
 **/

public class WrapperQuery {
    public static void main(String[] args) {
        // select * from table where id=2 or name = 'name3'
        BoolQueryBuilder parentQueryBuilder = QueryBuilders.boolQuery();
        parentQueryBuilder.should(QueryBuilders.termsQuery("id", "2"));
        parentQueryBuilder.should(QueryBuilders.termsQuery("name", "name3"));

        // select * from table where (id=1 and name=name1) or (id=2 and name2=name2)
        BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
        subQuery.must(QueryBuilders.termsQuery("id", "1"));
        subQuery.must(QueryBuilders.termsQuery("name", "name1"));
        BoolQueryBuilder subQuery2 = QueryBuilders.boolQuery();
        subQuery2.must(QueryBuilders.termsQuery("id2", "2"));
        subQuery2.must(QueryBuilders.termsQuery("name2", "name2"));
        parentQueryBuilder.should(subQuery);
        parentQueryBuilder.should(subQuery2);
    }
}
