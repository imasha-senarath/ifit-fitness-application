package com.example.ifitcoach;

public class Orders
{
    public String buyer, orderStatus, seller, serviceID, date, time;

    public Orders()
    {

    }

    public Orders(String buyer, String orderStatus, String seller, String serviceID, String date, String time) {
        this.buyer = buyer;
        this.orderStatus = orderStatus;
        this.seller = seller;
        this.serviceID = serviceID;
        this.date = date;
        this.time = time;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
