package com.tesco.services.adapters.sonetto.Elements;

import javax.xml.bind.annotation.*;

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
        /*Empty Constructor*/
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

}
