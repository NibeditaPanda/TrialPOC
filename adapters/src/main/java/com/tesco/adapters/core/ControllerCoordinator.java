package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import org.slf4j.Logger;

import java.util.Date;

import static com.tesco.adapters.core.PriceKeys.PRICE_COLLECTION;
import static com.tesco.adapters.core.PriceKeys.PROMOTION_COLLECTION;
import static com.tesco.adapters.core.PriceKeys.STORE_COLLECTION;
import static org.slf4j.LoggerFactory.getLogger;

public class ControllerCoordinator {

    private static final Logger logger = getLogger(ControllerCoordinator.class);

    public void processData(Controller controller, DBCollection tempPriceCollection, DBCollection tempStoreCollection, DBCollection tempPromotionCollection) {
        try {
            controller.fetchAndSavePriceDetails();

            logger.info("Renaming Price collection....");
            tempPriceCollection.rename(PRICE_COLLECTION, true);

            logger.info("Renaming Store collection....");
            tempStoreCollection.rename(STORE_COLLECTION, true);

            logger.info("Renaming Promotion collection....");
            tempPromotionCollection.rename(PROMOTION_COLLECTION, true);

            logger.info("Successfully imported data for " + new Date());

        } catch (Exception exception) {
            logger.error("Error importing data", exception);
        }
    }
}
