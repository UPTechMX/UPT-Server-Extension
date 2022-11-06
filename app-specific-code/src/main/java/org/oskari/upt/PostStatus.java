package org.oskari.upt;

public class PostStatus {
    public String status;
    public String message;
    public PostStatus(String statuss,String messages) {
        status=statuss;
        message=messages;
    }
    public PostStatus() {
        status="";
        message="";
    }
}
