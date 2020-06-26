package com.example.ifitcoach;

public class UserServices
{
    public String userid, servicetitle, servicedate, servicetime, servicefirstimage, servicesummarydescription;

    public UserServices()
    {
    }

    public UserServices(String userid, String servicetitle, String servicedate, String servicetime, String servicefirstimage, String servicesummarydescription)
    {
        this.userid = userid;
        this.servicetitle = servicetitle;
        this.servicedate = servicedate;
        this.servicetime = servicetime;
        this.servicefirstimage = servicefirstimage;
        this.servicesummarydescription = servicesummarydescription;
    }

    public String getServicetitle()
    {
        return servicetitle;
    }

    public void setServicetitle(String servicetitle)
    {
        this.servicetitle = servicetitle;
    }

    public String getServicedate()
    {
        return servicedate;
    }

    public void setServicedate(String servicedate)
    {
        this.servicedate = servicedate;
    }

    public String getServicetime()
    {
        return servicetime;
    }

    public void setServicetime(String servicetime)
    {
        this.servicetime = servicetime;
    }

    public String getServicefirstimage()
    {
        return servicefirstimage;
    }

    public void setServicefirstimage(String servicefirstimage)
    {
        this.servicefirstimage = servicefirstimage;
    }

    public String getServicesummarydescription()
    {
        return servicesummarydescription;
    }

    public void setServicesummarydescription(String servicesummarydescription)
    {
        this.servicesummarydescription = servicesummarydescription;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
