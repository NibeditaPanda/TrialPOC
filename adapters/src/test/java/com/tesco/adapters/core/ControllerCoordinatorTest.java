package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ControllerCoordinatorTest {

    private ControllerCoordinator controllerCoordinator;

    @Mock
    private Controller controller;

    @Mock
    private DBCollection tempPriceDbCollection;

    @Mock
    private DBCollection tempStoreDbCollection;

    @Mock
    private DBCollection tempPromotionDbCollection;

    @Before
    public void setUp() throws Exception {
        controllerCoordinator = new ControllerCoordinator();
    }

    @Test
    public void shouldNotRenameCollectionGivenFileIsCorrupted() throws Exception {

        doThrow(new ColumnNotFoundException("Error")).when(controller).fetchAndSavePriceDetails();

        controllerCoordinator.processData(controller, tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection);

        verify(tempPriceDbCollection, never()).rename("prices");
        verify(tempStoreDbCollection, never()).rename("stores");
        verify(tempPromotionDbCollection, never()).rename("promotions");
        verify(controller).deleteRpmPriceZoneCsvFilePath();
        verify(controller).deleteRpmStoreZoneCsvFilePath();
        verify(controller).deleteRpmPromotionCsvFilePath();
        verify(controller).deleteRpmPromotionDescCSVUrl();
        verify(controller).deleteSonettoPromotionsXMLFilePath();

    }

    @Test
    public void shouldNotRenameCollectionGivenSonettoIsCorrupted() throws Exception {

        doThrow(new JAXBException("Error")).when(controller).fetchAndSavePriceDetails();

        controllerCoordinator.processData(controller, tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection);

        verify(tempPriceDbCollection, never()).rename("prices");
        verify(tempStoreDbCollection, never()).rename("stores");
        verify(tempPromotionDbCollection, never()).rename("promotions");
        verify(controller).deleteRpmPriceZoneCsvFilePath();
        verify(controller).deleteRpmStoreZoneCsvFilePath();
        verify(controller).deleteRpmPromotionCsvFilePath();
        verify(controller).deleteRpmPromotionDescCSVUrl();
        verify(controller).deleteSonettoPromotionsXMLFilePath();
    }

    @Test
    public void shouldNotRenameCollectionGivenSonettoMalformedXml() throws Exception {

        doThrow(new SAXException("Error")).when(controller).fetchAndSavePriceDetails();

        controllerCoordinator.processData(controller, tempPriceDbCollection, tempStoreDbCollection, tempPromotionDbCollection);

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
