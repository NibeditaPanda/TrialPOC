package com.tesco.services.adapters.rpm.writers;

import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Promotion;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.repositories.ProductRepository;

import java.util.Map;

public class ProductMapper {
    private ProductRepository productRepository;
    private  Product product;

    public ProductMapper(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    /** PS-238 Modified By Nibedita - add isNewProduct flag to check if the product for the input item is exists or not - Start*/

      public Product mapPriceZonePrice(Map<String, String> headerToValueMap,boolean isNewProduct) {
        return mapToProductForPriceZone(headerToValueMap, CSVHeaders.Price.PRICE_ZONE_ID, CSVHeaders.Price.PRICE_ZONE_PRICE,isNewProduct);
    }
    /** PS-238 Modified By Nibedita - add isNewProduct flag to check if the product for the input item is exists or not - End*/

      public Product mapPromoZonePrice(Map<String, String> headerToValueMap) {
        return mapToProductForPromoZone(headerToValueMap, CSVHeaders.Price.PROMO_ZONE_ID, CSVHeaders.Price.PROMO_ZONE_PRICE);
    }
    /** PS-238 Modified By Nibedita - add isNewProduct flag to check if the product for the input item is exists or not - Start*/
    private Product mapToProductForPriceZone(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader,boolean isNewProduct)
    {
    /** PS-238 Modified By Nibedita - add isNewProduct flag to check if the product for the input item is exists or not - End*/
        String itemHeader = CSVHeaders.Price.ITEM;
        String item = headerToValueMap.get(itemHeader);
        String tpnb = item.split("-")[0];
        String tpncHeader = CSVHeaders.Price.TPNC;
        String tpnc = headerToValueMap.get(tpncHeader);
        /**Added By Nibedita/Mukund - PS-112
         * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
        String sellingUOMHeader = CSVHeaders.Price.SELLING_UOM;
        String sellingUOM = headerToValueMap.get(sellingUOMHeader);

        /** PS-238 Modified By Nibedita - to construct a product if it is occured for the first time else the data for the product
         *  will get appended to already constructed product for the same item - Start*/
        if (isNewProduct) {
            product = new Product(tpnb);
        }
        ProductVariant productVariant = getProductVariant(product, tpnc);
        /** PS-238 Modified By Nibedita - to construct a product if it is occured for the first time else the data for the product
         *  will get appended to already constructed product for the same item - End*/

          /**Added By Nibedita - PS-112
         * Given the  price End Point,When the price rest calls are requested, then the response JSON should contain selling UOM for the tpnc line with IDL  */
        productVariant.setSellingUOM(sellingUOM);

        final int zoneId = Integer.parseInt(headerToValueMap.get(zoneIdHeader));
        productVariant.addSaleInfo(new SaleInfo(zoneId, headerToValueMap.get(priceHeader)));

        return product;
    }


  private Product mapToProductForPromoZone(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader) {
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
        else if (saleInfo != null && saleInfo.getPromotions().isEmpty())
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

        String tpnc = productRepository.getProductTPNC(item);

        ProductVariant productVariant = getProductVariant(product, tpnc);
        final int zoneId = Integer.parseInt(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.ZONE_ID));
        SaleInfo saleInfo = productVariant.getSaleInfo(zoneId);

        if(saleInfo != null) {
            String offerId = promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.OFFER_ID);
            Promotion promotion = saleInfo.getPromotionByOfferId(offerId);

            if(promotion!=null) {
                promotion.setCFDescription1(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC1));
                promotion.setCFDescription2(promotionDescInfoMap.get(CSVHeaders.PromoDescExtract.DESC2));
            }
        }

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
