package com.example.cyanhearth.wordfinder;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cyanhearth on 16/02/2016.
 */
public class Group {

    private int groupId;
    private String groupName;
    private ArrayList<Child> children;

    public Group(int groupId) {
        this.groupId = groupId;
        this.children = new ArrayList<>();
    }

    public void addChild(Child child) {
        children.add(child);
    }

    public void sort() {
        Collections.sort(children);
    }

    //getters
    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public ArrayList<Child> getChildren() {
        return children;
    }

    //setters
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setChildren(ArrayList<Child> children) {
        this.children = children;
    }
}
