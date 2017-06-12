package com.example.manar_000.raye7;

/**
 * Created by manar_000 on 6/11/2017.
 */

public class Duration {

    public  Duration (String text , int value){
        this.text = text;
        this.value = value ;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    String text ;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    int value ;

}
