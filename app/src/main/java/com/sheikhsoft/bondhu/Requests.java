package com.sheikhsoft.bondhu;

/**
 * Created by Sk Shamimul islam on 08/19/2017.
 */

public class Requests {

    String request_type;

    public Requests(){

    }
    public Requests(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }


}
