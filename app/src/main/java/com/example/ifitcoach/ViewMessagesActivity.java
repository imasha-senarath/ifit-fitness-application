package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMessagesActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, messagesDatabaseReference;

    private RecyclerView msgList;

    private EditText msgInput;
    private ImageButton sendMsgBtn;

    String intentUserImage, intentUsername;

    String currentUserID, intentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);

        Intent intent = getIntent();
        intentUserID = intent.getExtras().getString("intentUserID");

        firebaseAuth= FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Messages");

        msgInput = findViewById(R.id.view_msg_msg_input);
        sendMsgBtn = findViewById(R.id.view_msg_msg_send_button);

        msgList = (RecyclerView) findViewById(R.id.messages_list);
        msgList.setNestedScrollingEnabled(false);
        msgList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define comment order*/
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        msgList.setLayoutManager(linearLayoutManager);


        sendMsgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendMessage();
            }
        });

        toolbarUserImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToProfilePage();
            }
        });

        RetrieveIntentUserDetails();

        RetrieveAllMessages();

    }


    private void RetrieveAllMessages()
    {
        Query query = messagesDatabaseReference.child(currentUserID).child(intentUserID);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(query, Messages.class)
                .build();

        FirebaseRecyclerAdapter<Messages, MessageViewHolder> adapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Messages messages)
            {
                messageViewHolder.receiverSide.setVisibility(View.GONE);
                messageViewHolder.senderSide.setVisibility(View.GONE);

                String receiver = messages.getReceiver();
                String sender = messages.getSender();

                if(sender.equals(currentUserID))
                {
                    messageViewHolder.senderSide.setVisibility(View.VISIBLE);
                    messageViewHolder.senderMsg.setText(messages.getMessage());
                    messageViewHolder.senderMsgTime.setText(messages.getTime());
                }

                if(receiver.equals(currentUserID))
                {
                    messageViewHolder.receiverSide.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMsg.setText(messages.getMessage());
                    messageViewHolder.receiverMsgTime.setText(messages.getTime());
                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout,parent,false);
                MessageViewHolder messageViewHolder = new MessageViewHolder(view);
                return messageViewHolder;
            }
        };

        msgList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder
    {
        TextView receiverMsg, receiverMsgTime, senderMsg, senderMsgTime;
        LinearLayout receiverSide, senderSide;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            receiverMsg = itemView.findViewById(R.id.message_layout_receiver_message);
            receiverSide = itemView.findViewById(R.id.message_layout_receiver_side);
            receiverMsgTime = itemView.findViewById(R.id.message_layout_receiver_message_time);
            senderMsg = itemView.findViewById(R.id.message_layout_sender_message);
            senderSide = itemView.findViewById(R.id.message_layout_sender_side);
            senderMsgTime = itemView.findViewById(R.id.message_layout_sender_message_time);
        }
    }


    private void SendMessage()
    {
        if(!TextUtils.isEmpty(msgInput.getText().toString()))
        {
            /* get current date and time  */
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            String currentDate = simpleDateFormat.format(calendar.getTime());
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm aa");
            String currentTime = simpleTimeFormat.format(calendar.getTime());

            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMdd");
            String date = simpleDateFormat2.format(calendar.getTime());
            SimpleDateFormat simpleTimeFormat2 = new SimpleDateFormat("HHmmss");
            String time = simpleTimeFormat2.format(calendar.getTime());
            String randomMsgID = date + time;



            HashMap msgMap1 = new HashMap();
            msgMap1.put("sender",currentUserID);
            msgMap1.put("receiver",intentUserID);
            msgMap1.put("message",msgInput.getText().toString());
            msgMap1.put("time",currentDate+" • "+currentTime);
            messagesDatabaseReference.child(currentUserID).child(intentUserID).child(randomMsgID).updateChildren(msgMap1);

            HashMap msgMap2 = new HashMap();
            msgMap2.put("sender",currentUserID);
            msgMap2.put("receiver",intentUserID);
            msgMap2.put("message",msgInput.getText().toString());
            msgMap2.put("time",currentDate+" • "+currentTime);
            messagesDatabaseReference.child(intentUserID).child(currentUserID).child(randomMsgID).updateChildren(msgMap2);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(false);
            linearLayoutManager.setStackFromEnd(true);
            msgList.setLayoutManager(linearLayoutManager);

            msgInput.setText("");
        }
    }

    private void RetrieveIntentUserDetails()
    {
        userDatabaseReference.child(intentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username"))
                    {
                        intentUsername = dataSnapshot.child("username").getValue().toString();
                        if(!intentUsername.isEmpty())
                        {
                            getSupportActionBar().setTitle(intentUsername);
                        }
                    }

                    if(dataSnapshot.hasChild("userimage"))
                    {
                        intentUserImage = dataSnapshot.child("userimage").getValue().toString();
                        if(!intentUserImage.isEmpty())
                        {
                            Picasso.with(getApplication()).load(intentUserImage).placeholder(R.drawable.profile_image_placeholder).into(toolbarUserImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void UserSendToProfilePage()
    {
        Intent mainIntent = new Intent(ViewMessagesActivity.this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
        mainIntent.putExtra("intentUserID", intentUserID);
        startActivity(mainIntent);
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
