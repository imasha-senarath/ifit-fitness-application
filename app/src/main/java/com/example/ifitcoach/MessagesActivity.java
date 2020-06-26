package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, messagesDatabaseReference;

    private RecyclerView chatList;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        /* Adding tool bar & title to add service activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth= FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);

        chatList = (RecyclerView) findViewById(R.id.chat_list);
        chatList.setNestedScrollingEnabled(false);
        chatList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define comment order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(linearLayoutManager);

        RetrieveAllChats();

    }


    private void RetrieveAllChats()
    {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(messagesDatabaseReference, Messages.class)
                .build();

      FirebaseRecyclerAdapter<Messages, ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Messages, ChatViewHolder>(options)
      {
          @Override
          protected void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i, @NonNull Messages messages)
          {
              /* getting user ID */
              final String userID = getRef(i).getKey();
              if(!userID.isEmpty())
              {
                    userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.hasChild("username"))
                                {
                                    String name = dataSnapshot.child("username").getValue().toString();
                                    if(!name.isEmpty())
                                    {
                                        chatViewHolder.userName.setText(name);
                                    }
                                }

                                if(dataSnapshot.hasChild("userimage"))
                                {
                                    String image = dataSnapshot.child("userimage").getValue().toString();
                                    if(!image.isEmpty())
                                    {
                                        Picasso.with(MessagesActivity.this).load(image).placeholder(R.drawable.default_user_image).into(chatViewHolder.userImage);

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

              chatViewHolder.itemView.setOnClickListener(new View.OnClickListener()
              {
                  @Override
                  public void onClick(View v)
                  {
                      Intent viewMessagesIntent = new Intent(getApplication(), ViewMessagesActivity.class);
                      viewMessagesIntent.putExtra("intentUserID", userID);
                      startActivity(viewMessagesIntent);
                  }
              });
          }

          @NonNull
          @Override
          public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
          {
              View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout,parent,false);
              ChatViewHolder chatViewHolder = new ChatViewHolder(view);
              return chatViewHolder;
          }
      };

        chatList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        CircleImageView userImage;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userImage = (CircleImageView) itemView.findViewById(R.id.connection_userimage);
            userName = (TextView) itemView.findViewById(R.id.connection_username);
        }
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
