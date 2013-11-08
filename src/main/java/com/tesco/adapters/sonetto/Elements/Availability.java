package com.tesco.adapters.sonetto.Elements;

import javax.xml.bind.annotation.XmlElement;

public class Availability {

    public Availability() {
    }

    public Availability(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @XmlElement(name = "StartDate")
    private String startDate;

    @XmlElement(name = "EndDate")
    private String endDate;

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
