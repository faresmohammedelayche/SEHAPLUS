package com.example.sehaplus;

public class Nurse {
    String firstName, lastName ,speciality,profileImage;
    Long rating,price;

    public Nurse(){}

    public Nurse(String firstName, String lastName, String speciality, String profileImage, Long rating, Long price) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.speciality = speciality;
        this.profileImage = profileImage;
        this.rating = rating;
        this.price = price;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
