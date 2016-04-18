package com.example.cyanhearth.wordfinder;

import android.support.annotation.NonNull;

public class Child implements Comparable<Child> {

    private String childName;
    private String childStrippedName;
    private int childScore;

    public Child(String childName, String childStrippedName, int childScore) {
        this.childName = childName;
        this.childStrippedName = childStrippedName;
        this.childScore = childScore;
    }

    @Override
    public int compareTo(@NonNull Child another) {
        int compare = this.childStrippedName.compareTo(another.getChildName());
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

    public String getChildStrippedName() {
        return childStrippedName;
    }

    public int getChildScore()
    {
        return childScore;
    }

    //setters
    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setChildStrippedName(String childStrippedName) {
        this.childStrippedName = childStrippedName;
    }

    public void setChildScore(int childScore) {
        this.childScore = childScore;
    }

}
