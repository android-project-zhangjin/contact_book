package com.example.contact_book;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Arrays;
import java.util.List;

public class MyDiffCallBack extends DiffUtil.Callback {

    private final List<miniCard> oldList;
    private final List<miniCard> newList;

    MyDiffCallBack(List<miniCard> oldList, List<miniCard> newList){
        this.oldList=oldList;
        this.newList=newList;
    }
    @Override
    public int getOldListSize() {
        return oldList!=null?oldList.size():0;
    }

    @Override
    public int getNewListSize() {
        return newList!=null?newList.size():0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        miniCard oldItem=oldList.get(oldItemPosition);
        miniCard newItem=newList.get(newItemPosition);
        return oldItem.equals(newItem);
    }

    /**
     *判断内容是否相同
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        miniCard oldItem=oldList.get(oldItemPosition);
        miniCard newItem=newList.get(newItemPosition);

        if(oldItem.avatar == null){
            if(newItem.avatar == null){
                return oldItem.name.equals(newItem.name) &&
                        oldItem.relationship.equals(newItem.relationship) &&
                        oldItem.phoneType.equals(newItem.phoneType);
            } else {
                return false;
            }
        } else {
            if(newItem.avatar == null)
                return false;
            else {
                return oldItem.name.equals(newItem.name) &&
                        Arrays.equals(oldItem.avatar, newItem.avatar) &&
                        oldItem.relationship.equals(newItem.relationship) &&
                        oldItem.phoneType.equals(newItem.phoneType);
            }
        }

    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition);
    }
}