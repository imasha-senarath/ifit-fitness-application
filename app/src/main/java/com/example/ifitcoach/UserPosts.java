package com.example.ifitcoach;

public class UserPosts
{
    public String userid, posttime, postdate, postdescription, postfirstimage, postsecondimage, postthirdimage;

    public UserPosts()
    {

    }

    public UserPosts(String userid, String posttime, String postdate, String postdescription, String postfirstimage,
                     String postsecondimage, String postthirdimage)
    {
        this.userid = userid;
        this.posttime = posttime;
        this.postdate = postdate;
        this.postdescription = postdescription;
        this.postfirstimage = postfirstimage;
        this.postsecondimage = postsecondimage;
        this.postthirdimage = postthirdimage;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPosttime() {
        return posttime;
    }

    public void setPosttime(String posttime) {
        this.posttime = posttime;
    }

    public String getPostdate() {
        return postdate;
    }

    public void setPostdate(String postdate) {
        this.postdate = postdate;
    }

    public String getPostdescription() {
        return postdescription;
    }

    public void setPostdescription(String postdescription) {
        this.postdescription = postdescription;
    }

    public String getPostfirstimage() {
        return postfirstimage;
    }

    public void setPostfirstimage(String postfirstimage) {
        this.postfirstimage = postfirstimage;
    }

    public String getPostsecondimage() {
        return postsecondimage;
    }

    public void setPostsecondimage(String postsecondimage) {
        this.postsecondimage = postsecondimage;
    }

    public String getPostthirdimage() {
        return postthirdimage;
    }

    public void setPostthirdimage(String postthirdimage) {
        this.postthirdimage = postthirdimage;
    }
}
