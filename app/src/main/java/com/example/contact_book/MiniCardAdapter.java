package com.example.contact_book;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MiniCardAdapter extends RecyclerView.Adapter<MiniCardAdapter.ViewHolder> {
    private List<MiniCard> contactList;

    private final Context mContext;

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

        //Item点击/长按事件监听器
        private final MyItemClickListener myItemClickListener;
        private final MyItemLongClickListener myItemLongClickListener;

        private ViewHolder(View view, MyItemClickListener listener, MyItemLongClickListener listener_long){
            super(view);
            //为组件绑定View
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

    MiniCardAdapter(List<MiniCard> cardList, Context context){
        contactList=cardList;
        mContext=context;
    }

    public void setData(List<MiniCard> _contactList){
        contactList=_contactList;
    }


    @Override
    @NotNull
    public MiniCardAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewtype){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.minicard_item,parent,false);
        return new ViewHolder(view,myItemClickListener,myItemLongClickListener);
    }

    @Override
    public int getItemCount(){return contactList.size();}

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder,int position){
        setTextVisibilityForView(contactList.get(position), holder);

        if (contactList.get(position).avatar != null){
            holder.avatarImageView.setImageBitmap(BitmapFactory.decodeByteArray(contactList.get(position).avatar,0,contactList.get(position).avatar.length));
        } else {
            holder.avatarImageView.setImageDrawable(mContext.getDrawable(R.mipmap.default_avatar));
        }

        contentSetVisibility(holder);
    }

    /**
     * 设置组件的Visibility
     * @param tmp Item
     * @param holder ViewHolder
     */
    private void setTextVisibilityForView(MiniCard tmp, ViewHolder holder){
        holder.nameTextView.setText(tmp.name);
        holder.phoneTextView.setText(tmp.phone);
        holder.phoneTypeTextView.setText(tmp.phoneType);
        holder.relationshipTextView.setText(tmp.relationship);

        //当对应Text为空时，设置Visibility为GONE；否则为Visible

        holder.nicknameTextView.setText(tmp.nickname);
        if(tmp.nickname == null || tmp.nickname.equals("")){
            holder.nicknameLabel.setVisibility(View.GONE);
            holder.nicknameTextView.setVisibility(View.GONE);
        } else {
            holder.nicknameLabel.setVisibility(View.VISIBLE);
            holder.nicknameTextView.setVisibility(View.VISIBLE);
        }

        holder.companyTextView.setText(tmp.company);
        if(tmp.company == null || tmp.company.equals("")){
            holder.companyTextView.setVisibility(View.GONE);
            holder.companyLabel.setVisibility(View.GONE);
        } else {
            holder.companyTextView.setVisibility(View.VISIBLE);
            holder.companyLabel.setVisibility(View.VISIBLE);
        }

        holder.emailTextView.setText(tmp.email);
        if(tmp.email==null || tmp.email.equals("")){
            holder.emailTextView.setVisibility(View.GONE);
            holder.emailLabel.setVisibility(View.GONE);
        } else {
            holder.emailTextView.setVisibility(View.VISIBLE);
            holder.emailLabel.setVisibility(View.VISIBLE);
        }

        holder.remarkTextView.setText(tmp.remark);
        if(tmp.remark==null || tmp.remark.equals("")){
            holder.remarkTextView.setVisibility(View.GONE);
        } else {
            holder.remarkTextView.setVisibility(View.VISIBLE);
        }

        holder.addressTextView.setText(tmp.address);
        if(tmp.address==null || tmp.address.equals("")){
            holder.addressTextView.setVisibility(View.GONE);
            holder.addressLabel.setVisibility(View.GONE);
        } else {
            holder.addressTextView.setVisibility(View.VISIBLE);
            holder.addressLabel.setVisibility(View.VISIBLE);
        }

        holder.noteTextView.setText(tmp.note);
        if(tmp.note==null || tmp.note.equals("")){
            holder.noteTextView.setVisibility(View.GONE);
            holder.noteLabel.setVisibility(View.GONE);
        } else {
            holder.noteTextView.setVisibility(View.VISIBLE);
            holder.noteLabel.setVisibility(View.VISIBLE);
        }

        switch(tmp.star){
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
    }

    /**
     *MyDiffUtil检测内容差异进行局部更新——主要用于提高图像的处理能力
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty()){
            onBindViewHolder(holder,position);
            return;
        }
        MiniCard tmp = (MiniCard) payloads.get(0);
        holder.avatarImageView.setImageBitmap(BitmapFactory.decodeByteArray(tmp.avatar,0,tmp.avatar.length));
        setTextVisibilityForView(tmp,holder);
        contentSetVisibility(holder);
    }

    private void contentSetVisibility(ViewHolder holder){
        //长按出现的按钮默认不可见
        holder.callButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.tickleButton.setVisibility(View.GONE);

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