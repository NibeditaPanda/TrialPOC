package com.tesco.services.repositories;

import com.tesco.core.DataGridResource;
import com.tesco.services.Promotion;
import org.infinispan.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.tesco.services.builder.PromotionBuilder.aPromotion;
import static java.util.UUID.randomUUID;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionRepositoryTest {

    private Cache<String,Object> promotionCache;
    private PromotionRepository promotionRepository;
    private DataGridResource dataGridResource;

    @Before
    public void setUp() throws Exception {
        dataGridResource = new DataGridResource();
        promotionCache = dataGridResource.getPromotionCache();

        promotionRepository = new PromotionRepository(promotionCache);

        Promotion promotion = aPromotion().offerId("offerId").build();

        promotionCache.put(randomUUID().toString(), promotion);
    }

    @After
    public void tearDown() throws Exception {
        dataGridResource.stop();
    }

    @Test
    public void getPromotionsByOfferId() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferId("offerId");

        assertThat(promotions).hasSize(1);
        assertThat(promotions.get(0).getOfferId()).isEqualTo("offerId");
    }

    @Test
    public void promotionsByOfferIdNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferId("something wrong");

        assertThat(promotions).hasSize(0);
    }
}
