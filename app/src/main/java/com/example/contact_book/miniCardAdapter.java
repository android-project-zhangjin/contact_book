package com.example.contact_book;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class miniCardAdapter extends RecyclerView.Adapter<miniCardAdapter.ViewHolder> {
    private List<miniCard> contactList;
    private Context mContext;

    private ViewHolder.MyItemClickListener myItemClickListener;
    private ViewHolder.MyItemLongClickListener myItemLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        TextView nameTextView;
        TextView nicknameTextView;
        TextView nicknameLabel;
        TextView phoneTextView;
        TextView phoneTypeTextView;

        TextView companyTextView;
        TextView companyLabel;
        TextView emailTextView;
        TextView emailLabel;
        TextView remarkTextView;

        TextView addressTextView;
        TextView addressLabel;
        TextView noteTextView;
        TextView noteLabel;
        TextView relationshipTextView;

        ImageView avatarImageView;
        Button starButton;
        Button editButton;
        Button deleteButton;
        Button callButton;

        Button tickleButton;



        private MyItemClickListener myItemClickListener;
        private MyItemLongClickListener myItemLongClickListener;

        private ViewHolder(View view, MyItemClickListener listener, MyItemLongClickListener listener_long){
            super(view);
            nicknameLabel=view.findViewById(R.id.nicknameLabel);
            companyLabel=view.findViewById(R.id.companyLabel);
            emailLabel=view.findViewById(R.id.emailLabel);
            addressLabel=view.findViewById(R.id.addressLabel);
            noteLabel=view.findViewById(R.id.noteLabel);

            nameTextView= view.findViewById(R.id.name_TextView);
            nicknameTextView=view.findViewById(R.id.nicknameText);
            phoneTextView= view.findViewById(R.id.phone_TextView);
            phoneTypeTextView=view.findViewById(R.id.phoneTypeText);
            companyTextView=view.findViewById(R.id.companyText);

            emailTextView=view.findViewById(R.id.emailText);
            remarkTextView=view.findViewById(R.id.remarkText);
            addressTextView=view.findViewById(R.id.addressText);
            noteTextView=view.findViewById(R.id.noteText);
            relationshipTextView= view.findViewById(R.id.relationship_TextView);

            avatarImageView= view.findViewById(R.id.avatarImageView);
            starButton= view.findViewById(R.id.star_btn_miniView);
            editButton=view.findViewById(R.id.editButton);
            deleteButton=view.findViewById(R.id.deleteButton);
            callButton=view.findViewById(R.id.callButton);
            tickleButton=view.findViewById(R.id.tickleButton);
            starButton.setOnClickListener(this);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            callButton.setOnClickListener(this);
            tickleButton.setOnClickListener(this);


            myItemClickListener=listener;
            myItemLongClickListener=listener_long;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View view) {
            //int position=(int)view.getTag();
            if(myItemClickListener!=null){
                myItemClickListener.onItemClick(view,view.getId(),getLayoutPosition());

            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(myItemLongClickListener!=null){
                myItemLongClickListener.onItemLongClick(view,R.id.miniCardView,getLayoutPosition());
            }
            //返回值为true时，不会触发同一view的onClick()事件；为false时，会同时触发
            return true;
        }

        public interface MyItemClickListener{
            void onItemClick(View view, int id, int position);
        }
        public interface MyItemLongClickListener{
            void onItemLongClick(View view,int id,int position);
        }
    }

    miniCardAdapter(List<miniCard> cardList, Context context){
        contactList=cardList;
        mContext=context;
    }

    public void setData(List<miniCard> _contactList){
        contactList=_contactList;
    }



    @Override
    @NotNull
    public miniCardAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewtype){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.minicard_item,parent,false);

        //View view=View.inflate(context,R.layout.minicard_item,null);
        return new ViewHolder(view,myItemClickListener,myItemLongClickListener);
    }

    @Override
    public int getItemCount(){return contactList.size();}

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder,int position){
        holder.nameTextView.setText(contactList.get(position).name);
        holder.nicknameTextView.setText(contactList.get(position).nickname);
        if(contactList.get(position).nickname == null || contactList.get(position).nickname.equals("")){
            holder.nicknameLabel.setVisibility(View.GONE);
            holder.nicknameTextView.setVisibility(View.GONE);
        }

        holder.phoneTextView.setText(contactList.get(position).phone);
        holder.phoneTypeTextView.setText(contactList.get(position).phoneType);

        holder.companyTextView.setText(contactList.get(position).company);
        if(contactList.get(position).company == null || contactList.get(position).company.equals("")){
            holder.companyTextView.setVisibility(View.GONE);
            holder.companyLabel.setVisibility(View.GONE);
        }

        holder.emailTextView.setText(contactList.get(position).email);
        if(contactList.get(position).email==null || contactList.get(position).email.equals("")){
            holder.emailTextView.setVisibility(View.GONE);
            holder.emailLabel.setVisibility(View.GONE);
        }

        holder.remarkTextView.setText(contactList.get(position).remark);
        if(contactList.get(position).remark==null || contactList.get(position).remark.equals("")){
            holder.remarkTextView.setVisibility(View.GONE);
        }

        holder.addressTextView.setText(contactList.get(position).address);
        if(contactList.get(position).address==null || contactList.get(position).address.equals("")){
            holder.addressTextView.setVisibility(View.GONE);
            holder.addressLabel.setVisibility(View.GONE);
        }

        holder.noteTextView.setText(contactList.get(position).note);
        if(contactList.get(position).note==null || contactList.get(position).note.equals("")){
            holder.noteTextView.setVisibility(View.GONE);
            holder.noteLabel.setVisibility(View.GONE);
        }

        switch(contactList.get(position).star){
            case 0:
                holder.starButton.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.mipmap.star,null));
                break;
            case 1:
                holder.starButton.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.mipmap.star_yellow,null));
                break;
            case 2:
                holder.starButton.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.mipmap.star_black,null));
                break;
        }

        holder.relationshipTextView.setText(contactList.get(position).relationship);

        if (contactList.get(position).avatar != null){
            holder.avatarImageView.setImageBitmap(contactList.get(position).avatar);
        } else {
            holder.avatarImageView.setImageDrawable(mContext.getDrawable(R.mipmap.default_avatar));
        }

        contentSetVisibility(holder);
    }

    private void contentSetVisibility(ViewHolder holder){
        //长按出现的按钮默认不可见
        holder.callButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);

        //信息显示默认可见
        holder.nameTextView.setVisibility(View.VISIBLE);
        holder.phoneTextView.setVisibility(View.VISIBLE);
        holder.relationshipTextView.setVisibility(View.VISIBLE);
        holder.phoneTypeTextView.setVisibility(View.VISIBLE);
    }

    public void setOnClickListener(ViewHolder.MyItemClickListener listener){
        myItemClickListener=listener;
    }

    public void setOnLongClickListener(ViewHolder.MyItemLongClickListener listener){
        myItemLongClickListener=listener;
    }

}