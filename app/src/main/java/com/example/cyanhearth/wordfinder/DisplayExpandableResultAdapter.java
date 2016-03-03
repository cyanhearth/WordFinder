package com.example.cyanhearth.wordfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayExpandableResultAdapter extends BaseExpandableListAdapter {

    private ArrayList<Group> groups;
    private LayoutInflater inflater;

    public DisplayExpandableResultAdapter(Context context,ArrayList<Group> groups) {
        this.groups = groups;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Child> children = groups.get(groupPosition).getChildren();
        return children.size();
    }

    @Override
    public Group getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Child getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groups.get(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.group_view, parent, false);

            // initialize the viewholder
            groupHolder = new GroupHolder();
            groupHolder.groupTextView = (TextView) convertView.findViewById(R.id.groupTextView);
            convertView.setTag(groupHolder);
        }
        else {
            // recycle the view
            groupHolder = (GroupHolder) convertView.getTag();
        }

        // update the group item view
        Group group = getGroup(groupPosition);
        groupHolder.groupTextView.setText(group.getGroupName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, parent, false);

            // initialize the viewholder
            childHolder = new ChildHolder();
            childHolder.childTextView = (TextView) convertView.findViewById(R.id.childTextView);
            childHolder.childScoreView = (TextView) convertView.findViewById(R.id.childScoreView);
            convertView.setTag(childHolder);
        }
        else {
            // recycle the view
            childHolder =(ChildHolder) convertView.getTag();
        }

        // update the child item view
        Child child = getChild(groupPosition,childPosition);
        childHolder.childTextView.setText(child.getChildName());

        String score = String.valueOf(child.getChildScore());

        childHolder.childScoreView.setText(score);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // ViewHolder classes for smooth scrolling of the list
    private static class GroupHolder {
        TextView groupTextView;
    }

    private static class ChildHolder {
        TextView childTextView;
        TextView childScoreView;
    }
}