package org.example.aideEtdon.model;

public class MapLocation {
    private int id;
    private String name;
    private String type; // "Pharmacie" or "Urgence"
    private double latitude;
    private double longitude;

    public MapLocation() {}

    public MapLocation(String name, String type, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MapLocation(int id, String name, String type, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
