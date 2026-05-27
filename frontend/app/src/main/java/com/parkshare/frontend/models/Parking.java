package com.parkshare.frontend.models;

import java.io.Serializable;

public class Parking implements Serializable {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private double pricePerHour;
    private int totalSlots;
    private int availableSlots;
    private double rating;
    private String imageUrl;
    private String distance;
    private String description;
    private String openingHours;
    private boolean isOpen;

    public Parking(String id, String name, String address, double latitude, double longitude, 
                   double pricePerHour, int totalSlots, int availableSlots, double rating, 
                   String imageUrl, String description, String openingHours, boolean isOpen) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pricePerHour = pricePerHour;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
        this.openingHours = openingHours;
        this.isOpen = isOpen;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getPricePerHour() { return pricePerHour; }
    public int getTotalSlots() { return totalSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public String getDistance() { return distance; }
    public String getDescription() { return description; }
    public String getOpeningHours() { return openingHours; }
    public boolean isOpen() { return isOpen; }

    // Setters
    public void setDistance(String distance) { this.distance = distance; }
}