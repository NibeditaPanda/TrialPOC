package com.tesco.adapters.sonetto.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.xml.bind.annotation.*;

import static com.tesco.adapters.core.PriceKeys.*;
import static java.lang.String.format;

@XmlRootElement(name = "Promotion")
@XmlAccessorType(XmlAccessType.FIELD)
public class Promotion {

    @XmlAttribute(name = "InternetExclusive")
    private boolean internetExclusive = false;

    @XmlAttribute(name = "SonettoID")
    private String sonettoID;

    @XmlElement(name = "ShelfTalkerImage")
    private String shelfTalkerImage;

    @XmlElement(name = "OfferText")
    private String offetText;

    @XmlElement(name = "Availability")
    private Availability availability;

    public Promotion() {
    }

    public Promotion(String sonettoID, String shelfTalkerImage, boolean internetExclusive) {
        this.sonettoID = sonettoID;
        this.shelfTalkerImage = shelfTalkerImage;
        this.internetExclusive = internetExclusive;
    }

    public Promotion(String offerId, String shelfTalker, boolean internetExclusive, String startDate, String endDate, String offetText) {
        this(offerId, shelfTalker, internetExclusive);
        this.offetText = offetText;
        this.availability = new Availability(startDate, endDate);
    }

    public boolean isInternetExclusive() {
        return internetExclusive;
    }

    public DBObject buildInternetDBObject() {
        BasicDBObject promotionDBObject = new BasicDBObject();
        promotionDBObject.put(PROMOTION_OFFER_ID, sonettoID);
        promotionDBObject.put(PROMOTION_OFFER_TEXT, offetText);
        promotionDBObject.put(PROMOTION_START_DATE, availability.getStartDate());
        promotionDBObject.put(PROMOTION_END_DATE, availability.getEndDate());

        return promotionDBObject;
    }

    public DBObject buildStoreDBObject(String shelfTalkerImagePath) {
        BasicDBObject promotionDBObject = new BasicDBObject();
        promotionDBObject.put(PROMOTION_OFFER_ID, sonettoID);
        promotionDBObject.put(SHELF_TALKER_IMAGE, format(shelfTalkerImagePath, shelfTalkerImage));
        return promotionDBObject;
    }

}
