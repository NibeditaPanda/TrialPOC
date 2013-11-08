package com.tesco.services.repositories;

import com.google.common.base.Function;
import com.tesco.services.Promotion;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import java.util.List;

import static com.google.common.collect.Lists.transform;
import static com.tesco.core.PriceKeys.*;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class PromotionRepository {
    private Cache<String, Object> promotionCache;

    public PromotionRepository(Cache<String, Object> promotionCache) {
        this.promotionCache = promotionCache;
    }

    public List<Promotion> getPromotionsByOfferIdZoneIdAndItemNumber(String offerId, String itemNumber, String zoneId) {

        if(isEmpty(offerId) || isEmpty(itemNumber) || isEmpty(zoneId)) {
            return EMPTY_LIST;
        }

        SearchManager manager = Search.getSearchManager(promotionCache);

        QueryBuilder builder = manager.buildQueryBuilderForClass(Promotion.class).get();
        Query query = builder
                .bool()
                    .must(builder
                            .keyword().onField(PROMOTION_OFFER_ID)
                            .ignoreAnalyzer()
                            .matching(offerId)
                            .createQuery())
                    .must(builder
                            .keyword().onField(ZONE_ID)
                            .ignoreAnalyzer()
                            .matching(zoneId)
                            .createQuery())
                    .must(builder
                            .keyword().onField(ITEM_NUMBER)
                            .ignoreAnalyzer()
                            .matching(itemNumber)
                            .createQuery())
                .createQuery();

        CacheQuery cacheQuery = manager.getQuery(query, Promotion.class);

        return transform(cacheQuery.list(), new Function<Object, Promotion>() {
            @Override
            public Promotion apply(Object o) {
                return (Promotion) o;
            }
        });

    }
}
