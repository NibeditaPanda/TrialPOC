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

        Promotion promotion1 = aPromotion()
                .offerId("9999999")
                .itemNumber("111111111")
                .zoneId("20")
                .build();
        Promotion promotion2 = aPromotion()
                .offerId("8888888")
                .itemNumber("10000000")
                .zoneId("80")
                .build();

        promotionCache.put(randomUUID().toString(), promotion1);
        promotionCache.put(randomUUID().toString(), promotion2);
    }

    @Test
    public void getPromotionsByOfferIdZoneIdAndItemNumber() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "111111111", "20");

        assertThat(promotions).hasSize(1);

        Promotion promotion = promotions.get(0);

        assertThat(promotion.getOfferId()).isEqualTo("9999999");
        assertThat(promotion.getZoneId()).isEqualTo("20");
        assertThat(promotion.getItemNumber()).isEqualTo("111111111");
    }

    @Test
    public void getPromotionsByOfferIdNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("something wrong", "111111111", "20");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByItemNumberNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "something wrong", "20");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByZoneIdNotFound() throws Exception {
        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "111111111", "something wrong");

        assertThat(promotions).hasSize(0);
    }

    @After
    public void tearDown() throws Exception {
        dataGridResource.stop();
    }
}
