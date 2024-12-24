package com.example.realtimemessaging;

import static com.example.realtimemessaging.utils.AndroidUtil.getUserModelfromIntent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimemessaging.adapter.ChatRecyclerAdapter;
import com.example.realtimemessaging.adapter.SearchUserRecyclerAdapter;
import com.example.realtimemessaging.model.ChatMessageModel;
import com.example.realtimemessaging.model.ChatroomModel;
import com.example.realtimemessaging.model.UserModel;
import com.example.realtimemessaging.utils.AndroidUtil;
import com.example.realtimemessaging.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;

    ChatRecyclerAdapter adapter;
    EditText messageInput;
    ImageButton sendMessage_button;
    ImageButton back_button;
    TextView otherUsername;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get user model
        otherUser= getUserModelfromIntent(getIntent());
        chatroomId= FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput=findViewById(R.id.chat_message_input);
        sendMessage_button=findViewById(R.id.message_send_button);
        back_button=findViewById(R.id.backButton);
        otherUsername=findViewById(R.id.other_username);
        recyclerView =findViewById(R.id.chat_recycler_view);

        back_button.setOnClickListener((view -> {
            startActivity(new Intent(ChatActivity.this,MainActivity.class));
        }));
        otherUsername.setText(otherUser.getUsername());
        sendMessage_button.setOnClickListener((view -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));
        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }
    void setupChatRecyclerView(){

        Query query= FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options=new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter =new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager =new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }
    void sendMessageToUser(String message){

        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel=new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            messageInput.setText("");
                        }
                    }
                });
    }
    void getOrCreateChatroomModel(){

        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task ->  {

            if(task.isSuccessful()){
                chatroomModel= task.getResult().toObject(ChatroomModel.class);
                if(chatroomModel==null){
                    //chat with new user or first time chat
                    chatroomModel=new ChatroomModel(
                            chatroomId, Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }
}