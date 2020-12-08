package com.example.contact_book;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class miniCard implements Cloneable{
    public String name;
    public String nickname;
    public String phone;
    public String phoneType;
    public String company;
    public String email;
    public String remark;
    public String address;
    public String note;
    public int star=0;
    public String relationship;
    public byte[] avatar=null;

    miniCard(){}

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof miniCard){
            miniCard card = (miniCard) obj;
            return this.phone.equals(card.phone);
        } else return false;
    }

    @NonNull
    @Override
    public miniCard clone() throws CloneNotSupportedException {
        miniCard card = new miniCard();
        if (this.avatar == null){
            card.avatar = null;
        } else {
            card.avatar = this.avatar.clone();
        }
        card.name = this.name;
        card.nickname = this.nickname;
        card.phone = this.phone;
        card.note = this.note;
        card.company = this.company;
        card.phoneType = this.phoneType;
        card.relationship = this.relationship;
        card.address = this.address;
        card.remark = this.remark;
        card.email = this.email;
        card.star = this.star;
        return card;
    }
}