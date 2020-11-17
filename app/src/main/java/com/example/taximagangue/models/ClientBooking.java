package com.example.taximagangue.models;

public class ClientBooking {

    String idClient;
    String idDriver;
    String destination;
    String Origin;
    String  time;
    String km;
    String estatus;
    double originLat;
    double originLog;
    double destinationLat;
    double destinationLog;

    public ClientBooking(String idClient, String idDriver, String destination, String origin, String time, String km, String estatus, double originLat, double originLog, double destinationLat, double destinationLog) {
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.destination = destination;
        Origin = origin;
        this.time = time;
        this.km = km;
        this.estatus = estatus;
        this.originLat = originLat;
        this.originLog = originLog;
        this.destinationLat = destinationLat;
        this.destinationLog = destinationLog;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return Origin;
    }

    public void setOrigin(String origin) {
        Origin = origin;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLog() {
        return originLog;
    }

    public void setOriginLog(double originLog) {
        this.originLog = originLog;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLog() {
        return destinationLog;
    }

    public void setDestinationLog(double destinationLog) {
        this.destinationLog = destinationLog;
    }
}
