package com.tesco.services.adapters.rpm.writers;

import com.tesco.services.core.*;
import com.tesco.services.repositories.ProductRepository;

import java.util.Map;

public class ProductMapper {
    private ProductRepository productRepository;

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
        String tpncHeader = CSVHeaders.Price.TPNB;// Todo: to be changed to tpnc when we get tpnc
        String tpnc = headerToValueMap.get(tpncHeader);
        Product product = getProduct(tpnc.split("-")[0]);//TODO: Remove the splitting logic once TPNC is given in the CSV extracts

        ProductVariant productVariant = getProductVariant(product, tpnc);

        final int zoneId = Integer.parseInt(headerToValueMap.get(zoneIdHeader));
        productVariant.addSaleInfo(new SaleInfo(zoneId, headerToValueMap.get(priceHeader)));

        return product;
    }

    public Product mapPromotion(Map<String, String> promotionInfoMap) {
        String tpncHeader = CSVHeaders.Promotion.TPNB; // Todo: to be changed to tpnc when we get tpnc
        String tpnc = promotionInfoMap.get(tpncHeader);
        Product product = getProduct(tpnc.split("-")[0]);//TODO: Remove the splitting logic once TPNC is given in the CSV extracts
        ProductVariant productVariant = getProductVariant(product, tpnc);

        final int zoneId = Integer.parseInt(promotionInfoMap.get(CSVHeaders.Promotion.ZONE_ID));
        SaleInfo saleInfo = productVariant.getSaleInfo(zoneId);

        if (saleInfo == null) {
            saleInfo = new SaleInfo(zoneId, null);
            productVariant.addSaleInfo(saleInfo);
        }

        Promotion promotion = new Promotion();
        promotion.setOfferId(promotionInfoMap.get(CSVHeaders.Promotion.OFFER_ID));
        promotion.setOfferName(promotionInfoMap.get(CSVHeaders.Promotion.OFFER_NAME));
        promotion.setStartDate(promotionInfoMap.get(CSVHeaders.Promotion.START_DATE));
        promotion.setEndDate(promotionInfoMap.get(CSVHeaders.Promotion.END_DATE));
        saleInfo.addPromotion(promotion);

        return product;
    }

    private Product getProduct(String tpnb) {
        return productRepository.getByTPNB(tpnb).or(new Product(tpnb));
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
