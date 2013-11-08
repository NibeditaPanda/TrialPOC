package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.tesco.core.DataGridResource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ControllerCoordinatorTest {

    @Mock
    private Controller controller;

    @Mock
    private DBCollection priceDbCollection;

    @Mock
    private DBCollection storeDbCollection;

    @Mock
    private DBCollection promotionDbCollection;

    @Mock
    private DBCollection tempPriceDbCollection;

    @Mock
    private DBCollection tempStoreDbCollection;

    @Mock
    private DBCollection tempPromotionDbCollection;

    @Mock
    private DataGridResource dataGridResource;

    @Ignore("need to fix") // TODO Vyv need to create files and check they werent deleted
    @Test
    public void shouldNotRenameCollectionGivenFileIsCorrupted() throws Exception {

//        doThrow(new ColumnNotFoundException("Error")).when(controller).fetchAndSavePriceDetails();

        Controller importController = new Controller("", "", "", "", "", "", "", dataGridResource.getPromotionCache());

        importController.processData(tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection, false);

        verify(tempPriceDbCollection, never()).rename("prices");
        verify(tempStoreDbCollection, never()).rename("stores");
        verify(tempPromotionDbCollection, never()).rename("promotions");
        verify(controller).deleteRpmPriceZoneCsvFilePath();
        verify(controller).deleteRpmStoreZoneCsvFilePath();
        verify(controller).deleteRpmPromotionCsvFilePath();
        verify(controller).deleteRpmPromotionDescCSVUrl();
        verify(controller).deleteSonettoPromotionsXMLFilePath();

    }

    @Ignore("need to fix") // TODO Vyv need to create files and check they werent deleted
    @Test
    public void shouldNotRenameCollectionGivenSonettoIsCorrupted() throws Exception {

//        doThrow(new JAXBException("Error")).when(controller).fetchAndSavePriceDetails();
        Controller importController = new Controller("", "", "", "", "", "", "", dataGridResource.getPromotionCache());

        controller.processData(tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection, false);

        verify(tempPriceDbCollection, never()).rename("prices");
        verify(tempStoreDbCollection, never()).rename("stores");
        verify(tempPromotionDbCollection, never()).rename("promotions");
        verify(controller).deleteRpmPriceZoneCsvFilePath();
        verify(controller).deleteRpmStoreZoneCsvFilePath();
        verify(controller).deleteRpmPromotionCsvFilePath();
        verify(controller).deleteRpmPromotionDescCSVUrl();
        verify(controller).deleteSonettoPromotionsXMLFilePath();
    }

    @Ignore("need to fix") // TODO Vyv need to create files and check they werent deleted
    @Test
    public void shouldNotRenameCollectionGivenSonettoMalformedXml() throws Exception {

//        doThrow(new SAXException("Error")).when(controller).fetchAndSavePriceDetails();
        Controller importController = new Controller("", "", "", "", "", "", "", dataGridResource.getPromotionCache());

        controller.processData(tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection, false);

        verify(tempPriceDbCollection, never()).rename("prices");
        verify(tempStoreDbCollection, never()).rename("stores");
        verify(tempPromotionDbCollection, never()).rename("promotions");
        verify(controller).deleteRpmPriceZoneCsvFilePath();
        verify(controller).deleteRpmStoreZoneCsvFilePath();
        verify(controller).deleteRpmPromotionCsvFilePath();
        verify(controller).deleteRpmPromotionDescCSVUrl();
        verify(controller).deleteSonettoPromotionsXMLFilePath();
    }
}
