package com.tesco.services.repositories;

import com.tesco.services.core.Promotion;
import com.tesco.services.resources.TestConfiguration;
import org.infinispan.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Iterables.getFirst;
import static com.tesco.services.builder.PromotionBuilder.aPromotion;
import static org.fest.assertions.api.Assertions.assertThat;

public class PromotionRepositoryTest {

    private Cache<String,Promotion> promotionCache;
    private PromotionRepository promotionRepository;
    private DataGridResource dataGridResource;
    private String uniqueKeyForPromotion1;
    private String uniqueKeyForPromotion2;

    @Before
    public void setUp() throws Exception {
        dataGridResource = new DataGridResource(new TestConfiguration());
        promotionCache = dataGridResource.getPromotionCache();
        promotionCache.clear();

        UUIDGenerator uuidGenerator = new UUIDGenerator();
        uniqueKeyForPromotion1 = uuidGenerator.getUUID();
        uniqueKeyForPromotion2 = uuidGenerator.getUUID();

        promotionRepository = new PromotionRepository(uuidGenerator, promotionCache);
    }

    @Test
    public void addPromotion() throws Exception {
        promotionRepository.addPromotion(aPromotion().build());
        promotionRepository.addPromotion(aPromotion().build());

        Promotion promotion = getFirst(this.promotionCache.values(), null);

        assertThat(promotionCache.size()).isEqualTo(2);

        assertThat(promotion.getOfferId()).isEqualTo("offerId");
        assertThat(promotion.getZoneId()).isEqualTo("zoneId");
        assertThat(promotion.getItemNumber()).isEqualTo("itemNumber");
    }

    @Test
    public void updatePromotion() throws Exception {
        addPromotion1();
        addPromotion2();

        Promotion promotion = aPromotion().uniqueKey(uniqueKeyForPromotion1)
                .description1("edited description1")
                .description2("edited description2").build();

        promotionRepository.updatePromotion(uniqueKeyForPromotion1, promotion);

        Promotion editedPromotion = this.promotionCache.get(uniqueKeyForPromotion1);

        assertThat(promotionCache.size()).isEqualTo(2);
        assertThat(editedPromotion.getUniqueKey()).isEqualTo(uniqueKeyForPromotion1);
        assertThat(editedPromotion.getOfferId()).isEqualTo("offerId");
        assertThat(editedPromotion.getZoneId()).isEqualTo("zoneId");
        assertThat(editedPromotion.getItemNumber()).isEqualTo("itemNumber");
        assertThat(editedPromotion.getCFDescription1()).isEqualTo("edited description1");
        assertThat(editedPromotion.getCFDescription2()).isEqualTo("edited description2");

    }

    @Test
    public void getPromotionsByOfferIdZoneIdAndItemNumber() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "111111111", "20");

        assertThat(promotions).hasSize(1);

        Promotion promotion = promotions.get(0);

        assertThat(promotion.getOfferId()).isEqualTo("9999999");
        assertThat(promotion.getZoneId()).isEqualTo("20");
        assertThat(promotion.getItemNumber()).isEqualTo("111111111");
    }

    @Test
    public void getPromotionsByOfferIdNotFound() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("something wrong", "111111111", "20");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByItemNumberNotFound() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "something wrong", "20");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getPromotionsByZoneIdNotFound() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("9999999", "111111111", "something wrong");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getEmptyPromotionsGivenOneOfArgsEmpty() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("offerId", "itemNumber", "");

        assertThat(promotions).hasSize(0);
    }

    @Test
    public void getEmptyPromotionsGivenOneOfArgsNull() throws Exception {
        addPromotion1();
        addPromotion2();

        List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber("offerId", "itemNumber", null);

        assertThat(promotions).hasSize(0);
    }

    @After
    public void tearDown() throws Exception {
        dataGridResource.stop();
    }

    private void addPromotion1() {
        Promotion promotion1 = aPromotion()
                .offerId("9999999")
                .itemNumber("111111111")
                .zoneId("20")
                .uniqueKey(uniqueKeyForPromotion1)
                .build();
        promotionCache.put(uniqueKeyForPromotion1, promotion1);
    }

    private void addPromotion2() {
        Promotion promotion2 = aPromotion()
                .offerId("8888888")
                .itemNumber("10000000")
                .zoneId("80")
                .uniqueKey(uniqueKeyForPromotion2)
                .build();

        promotionCache.put(uniqueKeyForPromotion2, promotion2);
    }
}
