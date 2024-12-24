package com.example.realtimemessaging.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimemessaging.ChatActivity;
import com.example.realtimemessaging.R;
import com.example.realtimemessaging.model. ChatroomModel;
import com.example.realtimemessaging.model.UserModel;
import com.example.realtimemessaging.utils.AndroidUtil;
import com.example.realtimemessaging.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter< ChatroomModel, RecentChatRecyclerAdapter. ChatroomModelViewHolder> {
    Context context;
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions< ChatroomModel> options,android.content.Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull  ChatroomModelViewHolder holder, int position, @NonNull  ChatroomModel model) {

        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        boolean lastMessageSentByMe=model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        UserModel otherUserModel= task.getResult().toObject(UserModel.class);
                        holder.usernameText.setText(otherUserModel.getUsername());
                        holder.lastMessageTime.setText(FirebaseUtil.timestsmpToString(model.getLastMessageTimestamp()));
                        if (lastMessageSentByMe)
                            holder.lastMessageText.setText("You: "+model.getLastMessage());
                        else
                            holder.lastMessageText.setText(otherUserModel.getUsername()+": "+model.getLastMessage());

                        holder.itemView.setOnClickListener((view -> {
                            Intent intent= new Intent(context, ChatActivity.class);

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
                            context.startActivity(intent);
                        }));
                    }
                });
    }

    @NonNull
    @Override
    public  ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new  ChatroomModelViewHolder(view);
    }

    class  ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;
        public  ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText=itemView.findViewById(R.id.username_text);
            lastMessageText=itemView.findViewById(R.id.last_message_text);
            lastMessageTime=itemView.findViewById(R.id.last_message_time_text);
            profilePic=itemView.findViewById(R.id.profile_pic_imageView);
        }
    }
}
