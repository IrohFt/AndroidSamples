package com.company.多态;

public class Dog extends Anima {

    @Override
    protected void getName() {
        super.getName();
        System.out.println("狗狗......");
    }
}