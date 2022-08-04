package com.example.instagramclone.view.model;

public class Post {

    public String email,comment,downloadUrl;

    public Post(String email,String comment,String downloadUrl){
        this.comment = comment;
        this.downloadUrl = downloadUrl;
        this.email = email;
    }
}
