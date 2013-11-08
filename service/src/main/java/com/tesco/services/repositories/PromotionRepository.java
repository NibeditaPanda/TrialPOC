package com.tesco.services.repositories;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tesco.services.Promotion;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import java.util.List;

import static com.tesco.core.PriceKeys.PROMOTION_OFFER_ID;

public class PromotionRepository {
    private Cache<String, Object> promotionCache;

    public PromotionRepository(Cache<String, Object> promotionCache) {
        this.promotionCache = promotionCache;
    }

    public List<Promotion> getPromotionsByOfferId(String offerId) {
        SearchManager manager = Search.getSearchManager(promotionCache);

        QueryBuilder builder = manager.buildQueryBuilderForClass(Promotion.class).get();
        Query query = builder.keyword()
                .onField(PROMOTION_OFFER_ID)
                .ignoreAnalyzer()
                .matching(offerId)
                .createQuery();

        CacheQuery cacheQuery = manager.getQuery(query, Promotion.class);

        return Lists.transform(cacheQuery.list(), new Function<Object, Promotion>() {
            @Override
            public Promotion apply(Object o) {
                return (Promotion) o;
            }
        });

    }
}
