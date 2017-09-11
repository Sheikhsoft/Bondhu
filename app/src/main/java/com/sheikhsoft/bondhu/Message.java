package com.sheikhsoft.bondhu;

/**
 * Created by Sk Shamimul islam on 08/17/2017.
 */

public class Message {
    private String message;
    private String aeen;
    private String time;
    private String type;

    public Message(String message, String aeen, String time, String type) {
        this.message = message;
        this.aeen = aeen;
        this.time = time;
        this.type = type;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAeen() {
        return aeen;
    }

    public void setAeen(String aeen) {
        this.aeen = aeen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



}
