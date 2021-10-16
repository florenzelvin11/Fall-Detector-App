package org.elvincode.fall_detector_v2.models;

import android.net.Uri;

public class User {
    private String user_id;
    private String first_name;
    private String last_name;
    private String email;
    private String contact;
    private Integer month, date, year, age;
    private String stringAge;
    private String gender;
    private String health_state;
    private String condition;
    private boolean isExpanded;
    private Uri imgProfileDp;
    private String fav_num;
    private String emergencyPhoneNum;

    public User() {
    }

    public User(String user_id, String first_name, String last_name, String email, String contact) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.contact = contact;
        this.month = null;
        this.date = null;
        this.year = null;
        this.age = null;
        this.stringAge = null;
        this.gender = null;
        this.health_state = null;
        this.condition = null;
        this.isExpanded = false;
        this.fav_num = "0";
        this.emergencyPhoneNum = "";
    }

    public User(String user_id, String first_name, String last_name, String email, String contact, String stringAge, String gender, String health_state, String condition, Uri imgProfileDp) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.contact = contact;
        this.stringAge = stringAge;
        this.gender = gender;
        this.health_state = health_state;
        this.condition = condition;
        this.imgProfileDp = imgProfileDp;
        this.isExpanded = false;
        this.fav_num = "0";
        this.emergencyPhoneNum = "";
    }

    public User(String user_id, String first_name, String last_name, String stringAge, String gender, String health_state, String condition, boolean isExpanded) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.stringAge = stringAge;
        this.gender = gender;
        this.health_state = health_state;
        this.condition = condition;
        this.isExpanded = isExpanded;
        this.fav_num = "0";
        this.emergencyPhoneNum = "";
    }

    public User(String user_id, String first_name, String last_name, String stringAge, String gender, String health_state, String condition, Uri imgProfileDp, boolean isExpanded) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.stringAge = stringAge;
        this.gender = gender;
        this.health_state = health_state;
        this.condition = condition;
        this.imgProfileDp = imgProfileDp;
        this.isExpanded = isExpanded;
        this.fav_num = "0";
        this.emergencyPhoneNum = "";
    }

    public User(String user_id, String first_name, String last_name, String email, String contact, Integer month, Integer date, Integer year, Integer age, String gender, String condition) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.contact = contact;
        this.month = month;
        this.date = date;
        this.year = year;
        this.age = age;
        this.gender = gender;
        this.condition = condition;
        isExpanded = false;
        this.fav_num = "0";
        this.emergencyPhoneNum = "";
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getHealth_state() {
        return health_state;
    }

    public void setHealth_state(String health_state) {
        this.health_state = health_state;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Uri getImgProfileDp() {
        return imgProfileDp;
    }

    public void setImgProfileDp(Uri imgProfileDp) {
        this.imgProfileDp = imgProfileDp;
    }

    public String getStringAge() {
        return stringAge;
    }

    public void setStringAge(String stringAge) {
        this.stringAge = stringAge;
    }

    public String getFav_num() {
        return fav_num;
    }

    public void setFav_num(String fav_num) {
        this.fav_num = fav_num;
    }

    public String getEmergencyPhoneNum() {
        return emergencyPhoneNum;
    }

    public void setEmergencyPhoneNum(String emergencyPhoneNum) {
        this.emergencyPhoneNum = emergencyPhoneNum;
    }

    @Override
    public String toString() {
        return "User{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", health_state='" + health_state + '\'' +
                ", contact='" + contact + '\'' +
                ", age=" + age.toString() +
                ", gender='" + gender + '\'' +
                ", Condition='" + condition + '\'' +
                '}';
    }
}
