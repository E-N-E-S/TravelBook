package com.enesb.travelbook.model;

import java.io.Serializable;

public class Place implements Serializable { // Arraylisti Intent ile gönderebilmek icin böyle yaptik

    public String name;
    public Double latitude;
    public Double longitude;

    public Place(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
