package com.tesco.services.adapters.core;


import com.tesco.services.core.Product;
import com.tesco.services.core.Promotion;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.resources.TestConfiguration;
import org.infinispan.Cache;
import org.mockito.Mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ControllerWithTempFilesBuilder {

    private String rpmPriceZoneCsvFilePath = TestFiles.RPM_PRICE_ZONE_CSV_FILE_PATH;
    private String rpmStoreZoneCsvFilePath = TestFiles.RPM_STORE_ZONE_CSV_FILE_PATH;
    private String rpmPromotionCsvFilePath = TestFiles.RPM_PROMOTION_CSV_FILE_PATH;
    private String sonettoPromotionsXMLFilePath = TestFiles.SONETTO_PROMOTIONS_XML_FILE_PATH;
    private String rpmPromotionDescCSVPath = TestFiles.RPM_PROMOTION_DESC_CSV_FILE_PATH;

    @Mock
    private Cache<String, Promotion> promotionCache;

    @Mock
    private Cache<String, Product> productPriceCache;

    private File rpmPriceZoneCsvFile;
    private File rpmStoreZoneCsvFile;
    private File rpmPromotionCsvFile;
    private File sonettoPromotionsXMLFile;
    private File rpmPromotionDescCSVFile;

    public ImportJob build() throws IOException {
        if(rpmPriceZoneCsvFile == null){
            rpmPriceZoneCsvFile = createRpmPriceZoneTempFile();
            Files.copy(new File(rpmPriceZoneCsvFilePath).toPath(), new FileOutputStream(rpmPriceZoneCsvFile));
        }

        if (rpmStoreZoneCsvFile == null) {
            rpmStoreZoneCsvFile = createRpmStoreZoneCsvTempFile();
            Files.copy(new File(rpmStoreZoneCsvFilePath).toPath(), new FileOutputStream(rpmStoreZoneCsvFile));

        }

        if (rpmPromotionCsvFile == null) {
            rpmPromotionCsvFile = createRpmPromotionCsvTempFile();
            Files.copy(new File(rpmPromotionCsvFilePath).toPath(), new FileOutputStream(rpmPromotionCsvFile));
        }

        if (sonettoPromotionsXMLFile == null) {
            sonettoPromotionsXMLFile = createSonettoPromotionsXMLTempFile();
            Files.copy(new File(sonettoPromotionsXMLFilePath).toPath(), new FileOutputStream(sonettoPromotionsXMLFile));
        }

        if (rpmPromotionDescCSVFile == null) {
            rpmPromotionDescCSVFile = createRpmPromotionDescCSVTempFile();
            Files.copy(new File(rpmPromotionDescCSVPath).toPath(), new FileOutputStream(rpmPromotionDescCSVFile));
        }


        return new ImportJob(rpmPriceZoneCsvFile.getPath(), rpmStoreZoneCsvFile.getPath(),
                rpmPromotionCsvFile.getPath(), sonettoPromotionsXMLFile.getPath(), rpmPromotionDescCSVFile.getPath(),
                "", "", "", "", new DBFactory(new TestConfiguration()), null);
    }

    private File createRpmPromotionDescCSVTempFile() throws IOException {
        return File.createTempFile("rpmPromotionDescCSV", ".tmp");
    }

    private File createSonettoPromotionsXMLTempFile() throws IOException {
        return File.createTempFile("sonettoPromotionsXMLFile", ".tmp");
    }

    private File createRpmPromotionCsvTempFile() throws IOException {
        return File.createTempFile("rpmPromotionCsvFile", ".tmp");
    }

    private File createRpmStoreZoneCsvTempFile() throws IOException {
        return File.createTempFile("rpmStoreZoneCsvFile", ".tmp");
    }

    private File createRpmPriceZoneTempFile() throws IOException {
        return File.createTempFile("rpmPriceZoneCsvFile", ".tmp");
    }

    public ControllerWithTempFilesBuilder withFakeRpmPriceZoneCsvFile(String fileContents) throws IOException {
        rpmPriceZoneCsvFile = createRpmPriceZoneTempFile();
        new FileWriter(rpmPriceZoneCsvFile).append(new StringBuilder().append(fileContents));
        return this;
    }

    public ControllerWithTempFilesBuilder withFakeSonettoPromotionsXMLFile(String fileContents) throws IOException {
        sonettoPromotionsXMLFile = createSonettoPromotionsXMLTempFile();
        new FileWriter(sonettoPromotionsXMLFile).append(new StringBuilder().append(fileContents));
        return this;
    }

    public ControllerWithTempFilesBuilder withInvalidStoreZoneFile(String fileContents) throws IOException {
        rpmStoreZoneCsvFile = createRpmStoreZoneCsvTempFile();
        new FileWriter(rpmStoreZoneCsvFile).append(new StringBuilder().append(fileContents));
        return this;
    }

    public void deleteTempFiles(){
        rpmPriceZoneCsvFile.delete();
        rpmStoreZoneCsvFile.delete();
        rpmPromotionCsvFile.delete();
        sonettoPromotionsXMLFile.delete();
        rpmPromotionDescCSVFile.delete();
    }

    // DANGER!! DANGER!!
    // These gets should be used with extreme caution they WILL change after build() is called
    public File getRpmPriceZoneCsvFile() {
        return rpmPriceZoneCsvFile;
    }

    public File getRpmStoreZoneCsvFile() {
        return rpmStoreZoneCsvFile;
    }

    public File getRpmPromotionCsvFile() {
        return rpmPromotionCsvFile;
    }

    public File getSonettoPromotionsXMLFile() {
        return sonettoPromotionsXMLFile;
    }

    public File getRpmPromotionDescCSV() {
        return rpmPromotionDescCSVFile;
    }

}
