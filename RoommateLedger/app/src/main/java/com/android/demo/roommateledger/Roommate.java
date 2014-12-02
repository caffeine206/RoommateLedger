package com.android.demo.roommateledger;

/**
 * Created by Derek on 11/29/2014.
 */
public class Roommate {
    public int id;
    public String name;
    public double balance;

    public Roommate() {

    }

    @Override
    public String toString() {
        return this.name + ":   " + "$" + balance;
    }
}
