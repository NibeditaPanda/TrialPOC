package com.tesco.services.adapters.rpm.writers;

import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.core.Product;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.AsyncReadWriteProductRepository;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import com.tesco.services.utility.Dockyard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RPMWriter {
    //TODO: More logging statements
    private Logger logger = LoggerFactory.getLogger("RPM Import");

    private String sonettoPromotionsXMLFilePath;

    private ProductRepository productRepository;
    private AsyncReadWriteProductRepository asyncReadWriteProductRepository;
    private StoreRepository storeRepository;
    private PriceServiceCSVReader rpmPriceReader;
    private PriceServiceCSVReader rpmPromoPriceReader;
    private PriceServiceCSVReader storeZoneReader;
    private PriceServiceCSVReader rpmPromotionReader;
    private PriceServiceCSVReader rpmPromotionDescReader;

    private SonettoPromotionXMLReader sonettoPromotionXMLReader;
    private PromotionRepository promotionRepository;


    public RPMWriter(String sonettoPromotionsXMLFilePath,
                     SonettoPromotionXMLReader sonettoPromotionXMLReader,
                     PromotionRepository promotionRepository,
                     ProductRepository productRepository,
                     StoreRepository storeRepository,
                     PriceServiceCSVReader rpmPriceReader,
                     PriceServiceCSVReader rpmPromoPriceReader,
                     PriceServiceCSVReader storeZoneReader,
                     PriceServiceCSVReader rpmPromotionReader,
                     PriceServiceCSVReader rpmPromotionDescReader) throws IOException, ColumnNotFoundException {

        this.sonettoPromotionsXMLFilePath = sonettoPromotionsXMLFilePath;
        this.sonettoPromotionXMLReader = sonettoPromotionXMLReader;
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.rpmPriceReader = rpmPriceReader;
        this.rpmPromoPriceReader = rpmPromoPriceReader;
        this.storeZoneReader = storeZoneReader;
        this.rpmPromotionReader = rpmPromotionReader;
        this.rpmPromotionDescReader = rpmPromotionDescReader;
    }
    public void write() throws IOException, ParserConfigurationException, JAXBException, ColumnNotFoundException, SAXException {
        // Using Couchbase
        // ===============
        logger.info("Importing price zone prices into Couchbase");
        writePriceZonePrices();
        writePromoZonePrices();
        writePromotions();
        writePromotionsDesc();
        writeStoreZones();
        /*Added by Sushil PS-114 to access view created in couchbase- Start*/
        getCouchBaseView();
        /*Added by Sushil PS-114 to access view created in couchbase- End*/
    }

    private void writeStoreZones() throws IOException {
        Map<String, String> storeInfoMap;
        StoreMapper storeMapper = new StoreMapper(storeRepository);
        while((storeInfoMap = storeZoneReader.getNext()) !=  null) {
            final Store store = storeMapper.map(storeInfoMap);
            storeRepository.insertStore(store,new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void aVoid) {
                }
                @Override
                public void onException(Exception e) {
                }
            });
        }
    }

    private void writePriceZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPriceZonePrice(productInfoMap);
            /*Added by Nibedita - PS 78 - Store ITEM and TPNC key value - Start*/
            productRepository.mapTPNC_TPNB(productInfoMap.get("TPNC"),productInfoMap.get("ITEM"));
            /*Added by Nibedita - PS 78 - Store ITEM and TPNC key value - End*/
            /*Modified by Nibedita - for adding last_updated_date field in Product JSON document while import - Story 114 -Start*/
           /* DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date();
            String updated_date = dateFormat.format(date);*/
            product.setLast_updated_date(Dockyard.getSysDate("yyyyMMdd"));
            /*Modified by Nibedita - for adding last_updated_date field in Product JSON document while import - Story 114 -End*/
            productRepository.insertProduct(product,new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void aVoid) {
                }
                @Override
                public void onException(Exception e) {
                }
            });
        }
    }

    private void writePromoZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPromoPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromoZonePrice(productInfoMap);
            productRepository.insertProduct(product,new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void aVoid) {
                }
                @Override
                public void onException(Exception e) {
                }
            });
        }
    }

    private void writePromotions() throws IOException {
        Map<String, String> promotionInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((promotionInfoMap = rpmPromotionReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromotion(promotionInfoMap);
            productRepository.insertProduct(product,new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void aVoid) {
                }
                @Override
                public void onException(Exception e) {
                }
            });
        }

    }

    private void writePromotionsDesc() throws IOException {
        Map<String, String> promotionDescInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((promotionDescInfoMap = rpmPromotionDescReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromotionDescription(promotionDescInfoMap);
            productRepository.insertProduct(product,new Listener<Void, Exception>() {
                @Override
                public void onComplete(Void aVoid) {
                }
                @Override
                public void onException(Exception e) {
                }
            });
        }
    }
    /*Added by Sushil PS-114 - Start*/
    /**
     * This will get view information from couchbase and process those products which are not update for more than 2 days
     * @throws IOException
     */
    private void getCouchBaseView() throws IOException {
        productRepository.getViewResult(new Listener<Void, Exception>(){
            @Override
            public void onComplete(Void aVoid) {
            }
            @Override
            public void onException(Exception e) {
            }
        });
    }
    /*Added by Sushil PS-114 - End*/
}
