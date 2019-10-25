package com.example.kadir.agricultureprojectclientside.ShortCut;


import com.example.kadir.agricultureprojectclientside.datatypes.farmdata.Farm;

public class User {
    private String userName;
    private String userLastname;
    private String userEmail;
    private String userAddress;
    private String userPhone;
    private Farm[] farms;
    private String userId;


    public User(){

    }
    public User(String userName, String userLastname, String userEmail, String userAddress, String userPhone){
        this.userName = userName;
        this.userLastname = userLastname;
        this.userEmail = userEmail;
        this.userAddress = userAddress;
        this.userPhone = userPhone;
    }

    public Farm[] getFarms() {
        return farms;
    }

    public String getUserLastname() {
        return userLastname;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserLastname(String userLastname) {
        this.userLastname = userLastname;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setFarms(Farm[] farms) {
        this.farms = farms;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;

    }

    public String getUserName(){

        return this.userName;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
