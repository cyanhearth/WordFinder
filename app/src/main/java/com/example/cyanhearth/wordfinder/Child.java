package com.example.cyanhearth.wordfinder;

import android.text.SpannableStringBuilder;

/**
 * Created by cyanhearth on 16/02/2016.
 */
public class Child implements Comparable<Child> {

    private SpannableStringBuilder childName;
    private int childScore;

    public Child(SpannableStringBuilder childName, int childScore) {
        this.childName = childName;
        this.childScore = childScore;
    }

    @Override
    public int compareTo(Child another) {
        int compare = this.childName.toString().compareTo(another.getChildName().toString());
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
    public  SpannableStringBuilder getChildName() {
        return childName;
    }

    public int getChildScore()
    {
        return childScore;
    }

    //setters
    public void setChildName(SpannableStringBuilder childName) {
        this.childName = childName;
    }

    public void setChildScore(int childScore) {
        this.childScore = childScore;
    }

}
