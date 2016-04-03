package com.example.cyanhearth.wordfinder;

import android.support.annotation.NonNull;

/**
 * Created by cyanhearth on 16/02/2016.
 */
public class Child implements Comparable<Child> {

    private String childName;
    private int childScore;

    public Child(String childName, int childScore) {
        this.childName = childName;
        this.childScore = childScore;
    }

    @Override
    public int compareTo(@NonNull Child another) {
        int compare = this.childName.compareTo(another.getChildName());
        if (compare > 0) {
            return 1;
        }
        else if (compare < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    //getters
    public  String getChildName() {
        return childName;
    }

    public int getChildScore()
    {
        return childScore;
    }

    //setters
    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setChildScore(int childScore) {
        this.childScore = childScore;
    }

}
