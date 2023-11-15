package com.example.jonathan.androidkvm;

public class Key {

    String displayValue;
    String code;

    public Key(String code){
        this.displayValue=code;
        this.code=code;
    }

    public Key(String displayValue,String code){
        this.displayValue=displayValue;
        this.code=code;
    }
}
