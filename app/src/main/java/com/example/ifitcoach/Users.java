package com.example.ifitcoach;

public class Users
{
    public String username, userimage;

    public Users()
    {

    }

    public Users(String username, String userimage) {
        this.username = username;
        this.userimage = userimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }
}
