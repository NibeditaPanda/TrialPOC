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

        Promotion promotion = aPromotion()
                .offerId("offerId")
                .itemNumber("itemNumber")
                .zoneId("zoneId")
                .build();

        promotionCache.put(randomUUID().toString(), promotion);
    }

    @Test
    public void getPromotionsByOfferIdZoneIdAndItemNumber() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("offerId", "itemNumber", "zoneId");

        assertThat(promotions).hasSize(1);

        Promotion promotion = promotions.get(0);

        assertThat(promotion.getOfferId()).isEqualTo("offerId");
        assertThat(promotion.getZoneId()).isEqualTo("zoneId");
        assertThat(promotion.getItemNumber()).isEqualTo("itemNumber");
    }

    @Test
    public void getPromotionsByOfferIdNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("something wrong", "itemNumber", "zoneId");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByItemNumberNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("offerId", "something wrong", "zoneId");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByZoneIdNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("offerId", "itemNumber", "something wrong");

        assertThat(promotions).hasSize(0);
    }

    @After
    public void tearDown() throws Exception {
        dataGridResource.stop();
    }
}
