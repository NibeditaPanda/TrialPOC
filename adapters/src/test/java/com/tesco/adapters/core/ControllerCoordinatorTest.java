package com.tesco.adapters.core;

import com.mongodb.DBCollection;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    }
}
