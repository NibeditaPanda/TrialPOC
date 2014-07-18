package com.tesco.services.adapters.rpm.writers;

import com.tesco.couchbase.listeners.Listener;
import com.tesco.services.core.*;
import com.tesco.services.repositories.AsyncReadWriteProductRepository;
import com.tesco.services.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProductMapper {
    private ProductRepository productRepository;
    private AsyncReadWriteProductRepository asyncReadWriteProductRepository;
    private Logger logger = LoggerFactory.getLogger("RPM Import");

    public ProductMapper(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product mapPriceZonePrice(Map<String, String> headerToValueMap) {
        return mapToProduct(headerToValueMap, CSVHeaders.Price.PRICE_ZONE_ID, CSVHeaders.Price.PRICE_ZONE_PRICE);
    }

    public Product mapPromoZonePrice(Map<String, String> headerToValueMap) {
        return mapToProduct(headerToValueMap, CSVHeaders.Price.PROMO_ZONE_ID, CSVHeaders.Price.PROMO_ZONE_PRICE);
    }

    private Product mapToProduct(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader) {
        String itemHeader = CSVHeaders.Price.ITEM;
        String item = headerToValueMap.get(itemHeader);
        String tpncHeader = CSVHeaders.Price.TPNC;
        String tpnc = headerToValueMap.get(tpncHeader);
        Product product = getProduct(item.split("-")[0]);

        ProductVariant productVariant = getProductVariant(product, tpnc);
        final int zoneId = Integer.parseInt(headerToValueMap.get(zoneIdHeader));
        productVariant.addSaleInfo(new SaleInfo(zoneId, headerToValueMap.get(priceHeader)));
        return product;
    }


    public Product mapPromotion(Map<String, String> promotionInfoMap) {
        String itemHeader = CSVHeaders.PromoExtract.ITEM;
        String item = promotionInfoMap.get(itemHeader);
        String tpncHeader = CSVHeaders.PromoExtract.TPNC;
        String tpnc = promotionInfoMap.get(tpncHeader);
        Product product = getProduct(item.split("-")[0]);
        //Product product = getProductIdentified(tpnc.split("-")[0]);

        ProductVariant productVariant = getProductVariant(product, tpnc);

        final int zoneId = Integer.parseInt(promotionInfoMap.get(CSVHeaders.PromoExtract.ZONE_ID));
        SaleInfo saleInfo = productVariant.getSaleInfo(zoneId);

        if (saleInfo == null) {
            saleInfo = new SaleInfo(zoneId, null);
            productVariant.addSaleInfo(saleInfo);
    }
        else if (saleInfo != null && saleInfo.getPromotions().size()==0)
        {
            Promotion promotion = new Promotion();
            promotion.setOfferId(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID));
            promotion.setOfferName(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_NAME));
            promotion.setEffectiveDate(promotionInfoMap.get(CSVHeaders.PromoExtract.START_DATE));
            promotion.setEndDate(promotionInfoMap.get(CSVHeaders.PromoExtract.END_DATE));
            saleInfo.addPromotion(promotion);
            productVariant.addSaleInfo(saleInfo);
        }
        else {
            Promotion promotion = new Promotion();
            promotion.setOfferId(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID));
            promotion.setOfferName(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_NAME));
            promotion.setEffectiveDate(promotionInfoMap.get(CSVHeaders.PromoExtract.START_DATE));
            promotion.setEndDate(promotionInfoMap.get(CSVHeaders.PromoExtract.END_DATE));
            saleInfo.addPromotion(promotion);
        }
        return product;
    }

    public Product mapPromotionDescription(Map<String, String> promotionDescInfoMap) {
        String itemHeader = CSVHeaders.PromoDescExtract.ITEM;
        String item = promotionDescInfoMap.get(itemHeader);
        Product product = getProduct(item.split("-")[0]);
        // Product product = getProductIdentified(tpnc.split("-")[0]);//TODO: Remove the splitting logic once TPNC is given in the CSV extracts

        String tpnc = productRepository.getProductTPNC(item);
        if (!productRepository.isSpaceOrNull(tpnc) && tpnc.length()==11)
             tpnc = productRepository.isSpaceOrNull(tpnc)?"":tpnc.substring(1,10);
        ProductVariant productVariant = getProductVariant(product, tpnc);
        final int zoneId = Integer.parseInt(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.ZONE_ID));
        SaleInfo saleInfo = productVariant.getSaleInfo(zoneId);

        if (saleInfo == null) {
            saleInfo = new SaleInfo(zoneId, null);
            productVariant.addSaleInfo(saleInfo);
        }

        String offerId = promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.OFFER_ID);
        Promotion promotion = saleInfo.getPromotionByOfferId(offerId);

        if(promotion == null) {
            promotion = new Promotion();
            promotion.setOfferId(offerId);
            saleInfo.addPromotion(promotion);
        }

        promotion.setCFDescription1(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC1));
        promotion.setCFDescription2(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC2));

        return product;
    }

    private Product getProduct(String tpnb) {
        return productRepository.getByTPNB(tpnb).or(new Product(tpnb));
    }

    private Product getProductIdentified(String tpnb) {
        Product productToBeInserted;
        productRepository.getProductByTPNB(tpnb, new Listener<Product, Exception>() {
            @Override
            public void onComplete(Product product) {
                if(product == null){
                    //Use Phaser or any future methods that is required
                }
                else{
                }
            }
            @Override
            public void onException(Exception e) {
            }
        }) ;
        if(productRepository.getProductIdentified()==null){
            productToBeInserted = new Product(tpnb);
        }
        else{
            productToBeInserted = productRepository.getProductIdentified();
        }
        return productToBeInserted;
    }

    private ProductVariant getProductVariant(Product product, String tpnc) {
        ProductVariant productVariant = product.getProductVariantByTPNC(tpnc);

        if (productVariant == null) {
            productVariant = new ProductVariant(tpnc);
            product.addProductVariant(productVariant);
        }
        return productVariant;
    }
}
