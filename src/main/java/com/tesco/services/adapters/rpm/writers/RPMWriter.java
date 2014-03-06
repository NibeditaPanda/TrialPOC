package com.tesco.services.adapters.rpm.writers;

import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.readers.PriceServiceCSVReader;
import com.tesco.services.adapters.sonetto.SonettoPromotionXMLReader;
import com.tesco.services.core.Product;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

public class RPMWriter {
    //TODO: More logging statements
    private Logger logger = LoggerFactory.getLogger("RPM Import");

    private String sonettoPromotionsXMLFilePath;

    private ProductRepository productRepository;
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
    }

    private void writeStoreZones() throws IOException {
        Map<String, String> storeInfoMap;
        StoreMapper storeMapper = new StoreMapper(storeRepository);

        while((storeInfoMap = storeZoneReader.getNext()) !=  null) {
            final Store store = storeMapper.map(storeInfoMap);
            storeRepository.put(store);
        }
    }

    private void writePriceZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPriceZonePrice(productInfoMap);
            productRepository.put(product);
        }
    }

    private void writePromoZonePrices() throws IOException {
        Map<String, String> productInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((productInfoMap = rpmPromoPriceReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromoZonePrice(productInfoMap);
            productRepository.put(product);
        }
    }

    private void writePromotions() throws IOException {
        Map<String, String> promotionInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((promotionInfoMap = rpmPromotionReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromotion(promotionInfoMap);
            productRepository.put(product);
        }

    }

    private void writePromotionsDesc() throws IOException {
        Map<String, String> promotionDescInfoMap;
        final ProductMapper productMapper = new ProductMapper(productRepository);

        while((promotionDescInfoMap = rpmPromotionDescReader.getNext()) !=  null) {
            final Product product = productMapper.mapPromotionDescription(promotionDescInfoMap);
            productRepository.put(product);
        }
    }
}
