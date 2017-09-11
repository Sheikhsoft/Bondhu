package com.sheikhsoft.bondhu;

/**
 * Created by Sk Shamimul islam on 08/20/2017.
 */

public class Chats {
    String from;
    String message;
    String seen;
    String time;
    public Chats(){

    }
    public Chats(String from, String message, String seen, String time, String type) {
        this.from = from;
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
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

    String type;
}
