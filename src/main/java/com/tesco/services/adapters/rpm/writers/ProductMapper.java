package com.tesco.services.adapters.rpm.writers;

import com.tesco.couchbase.listeners.Listener;
import com.tesco.services.core.*;
import com.tesco.services.repositories.AsyncReadWriteProductRepository;
import com.tesco.services.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductMapper {
    private ProductRepository productRepository;
    private AsyncReadWriteProductRepository asyncReadWriteProductRepository;
    private Logger logger = LoggerFactory.getLogger("RPM Import");

   /* Added by Salman,Rohan and Surya for PS-120 - Start   */
    private static List<String> priceExtractDataList = new ArrayList<String>();
   /* Added by Salman,Rohan and Surya for PS-120 - End   */

    public ProductMapper(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
   /* Modified by Salman,Rohan and Surya for PS-120 - Start   */
    public Product mapPriceZonePrice(Map<String, String> headerToValueMap) {
        return mapToProductForPriceZone(headerToValueMap, CSVHeaders.Price.PRICE_ZONE_ID, CSVHeaders.Price.PRICE_ZONE_PRICE);
    }

    public Product mapPromoZonePrice(Map<String, String> headerToValueMap) {
        return mapToProductForPromoZone(headerToValueMap, CSVHeaders.Price.PROMO_ZONE_ID, CSVHeaders.Price.PROMO_ZONE_PRICE);
    }

    private Product mapToProductForPriceZone(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader) {
        String itemHeader = CSVHeaders.Price.ITEM;
        String item = headerToValueMap.get(itemHeader);
        String tpnb = item.split("-")[0];
        String tpncHeader = CSVHeaders.Price.TPNC;
        String tpnc = headerToValueMap.get(tpncHeader);
        Product product;
       /* Modified by Salman,Rohan and Surya -This check is to Create a new Product from the Price Extracts Even though Promotions Exit as a part of Promotion Purge - Start*/
        if(priceExtractDataList.contains(tpnb)){
            product = getProduct(tpnb);
        }
        else{
            priceExtractDataList.add(tpnb);
            product = new Product(tpnb);
        }
       /* Modified by Salman,Rohan and Surya -This check is to Create a new Product from the Price Extracts Even though Promotions Exit as a part of Promotion Purge - End*/

        ProductVariant productVariant = getProductVariant(product, tpnc);
        final int zoneId = Integer.parseInt(headerToValueMap.get(zoneIdHeader));
        productVariant.addSaleInfo(new SaleInfo(zoneId, headerToValueMap.get(priceHeader)));
        return product;
    }

    private Product mapToProductForPromoZone(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader) {
        priceExtractDataList.clear();
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
   /* Modified by Salman,Rohan and Surya for PS-120 - End   */


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
/*Modified by Nibedita - to process the promotion construct for the product if the data is available in RPM and not in promo zone extract - PS-116 - Start*/
        if (saleInfo == null && promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID) == null) {
            saleInfo = new SaleInfo(zoneId, null);
            productVariant.addSaleInfo(saleInfo);
    }
       else  if (saleInfo == null && promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID) != null )
        {
            Promotion promotion = new Promotion();
            saleInfo = new SaleInfo(zoneId, null);
            promotion.setOfferId(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID));
            promotion.setZoneId(Integer.parseInt(promotionInfoMap.get(CSVHeaders.PromoExtract.ZONE_ID)));
            promotion.setOfferName(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_NAME));
            promotion.setEffectiveDate(promotionInfoMap.get(CSVHeaders.PromoExtract.START_DATE));
            promotion.setEndDate(promotionInfoMap.get(CSVHeaders.PromoExtract.END_DATE));
            saleInfo.addPromotion(promotion);
            productVariant.addSaleInfo(saleInfo);
        }
/*Modified by Nibedita - to process the promotion construct for the product if the data is available in RPM and not in promo zone extract - PS-116 - End*/
        else if (saleInfo != null && saleInfo.getPromotions().size()==0)
        {
            Promotion promotion = new Promotion();
            promotion.setOfferId(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID));
            promotion.setZoneId(Integer.parseInt(promotionInfoMap.get(CSVHeaders.PromoExtract.ZONE_ID)));
            promotion.setOfferName(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_NAME));
            promotion.setEffectiveDate(promotionInfoMap.get(CSVHeaders.PromoExtract.START_DATE));
            promotion.setEndDate(promotionInfoMap.get(CSVHeaders.PromoExtract.END_DATE));
            saleInfo.addPromotion(promotion);
            productVariant.addSaleInfo(saleInfo);
        }
        else {
            Promotion promotion = new Promotion();
            promotion.setOfferId(promotionInfoMap.get(CSVHeaders.PromoExtract.OFFER_ID));
            /*Added by Nibedita - to reflect zone id in JSON document for promotions - PS-116 -Start*/
            promotion.setZoneId(Integer.parseInt(promotionInfoMap.get(CSVHeaders.PromoExtract.ZONE_ID)));
            /*Added by Nibedita - to reflect zone id in JSON document for promotions - PS-116 -End*/
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
        /*Added by Nibedita - for PS 78 -  fetch tpnc based on item - Start*/
        String tpnc = productRepository.getProductTPNC(item);
        /*Added by Nibedita - for PS 78 -  fetch tpnc based on item - End*/
        ProductVariant productVariant = getProductVariant(product, tpnc);
        final int zoneId = Integer.parseInt(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.ZONE_ID));
        SaleInfo saleInfo = productVariant.getSaleInfo(zoneId);
    /*Modified by Nibedita - to process the promotion construct for the product if the data is available in RPM extract only - PS-116 - Start*/
     if(saleInfo != null) {
        String offerId = promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.OFFER_ID);
        Promotion promotion = saleInfo.getPromotionByOfferId(offerId);
        if(promotion!=null) {
            promotion.setCFDescription1(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC1));
            promotion.setCFDescription2(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC2));
        }
        else{
            saleInfo = null;
        }
     }
        /*Modified by Nibedita - to process the promotion construct for the product if the data is available in RPM extract only - PS-116 - End*/
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
