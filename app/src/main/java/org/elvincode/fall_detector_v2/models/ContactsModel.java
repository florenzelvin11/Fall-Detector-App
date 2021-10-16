package org.elvincode.fall_detector_v2.models;

public class ContactsModel {
    private String name;
    private String phoneNumber;
    private Boolean isExpanded;
    private Boolean isFavourite;

    public ContactsModel() {
    }

    public ContactsModel(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isExpanded = false;
        this.isFavourite = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        this.isExpanded = expanded;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        this.isFavourite = favourite;
    }
}
