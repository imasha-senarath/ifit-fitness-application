package com.example.ifitcoach;

public class PostComments
{
    public String userid, commentdescription, commentdate, commenttime;

    public PostComments()
    {

    }

    public PostComments(String userid, String commentdescription, String commentdate, String commenttime)
    {
        this.userid = userid;
        this.commentdescription = commentdescription;
        this.commentdate = commentdate;
        this.commenttime = commenttime;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCommentdescription() {
        return commentdescription;
    }

    public void setCommentdescription(String commentdescription) {
        this.commentdescription = commentdescription;
    }

    public String getCommentdate() {
        return commentdate;
    }

    public void setCommentdate(String commentdate) {
        this.commentdate = commentdate;
    }

    public String getCommenttime() {
        return commenttime;
    }

    public void setCommenttime(String commenttime) {
        this.commenttime = commenttime;
    }
}
