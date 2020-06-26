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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConnectionsActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectionsDatabaseReference, userDatabaseReference;

    private RecyclerView connectionsList;

    private String currentUserID;

    String intentFrom, intentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Connections");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("OtherUserProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        connectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections");

        connectionsList = (RecyclerView) findViewById(R.id.connections_list);
        connectionsList.setNestedScrollingEnabled(false);
        connectionsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define requests order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        connectionsList.setLayoutManager(linearLayoutManager);

        if(intentFrom.equals("OtherUserProfile") && !currentUserID.equals(intentUserID))
        {
            LoadConnections(intentUserID);
            GetUserDetails(intentUserID);
        }
        else
        {
            LoadConnections(currentUserID);
            GetUserDetails(currentUserID);
        }

    }

    private void GetUserDetails(String userID)
    {
        userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("username"))
                {
                    String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                    if(intentFrom.equals("OtherUserProfile"))
                    {
                        if(!TextUtils.isEmpty(retrieveUserName))
                        {
                            /* getting first name */
                            String arr[] = retrieveUserName.split(" ", 2);
                            getSupportActionBar().setTitle(arr[0]+"'s"+" Connections");
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


    private void LoadConnections(String userID)
    {
        Query query = connectionsDatabaseReference.child(userID).orderByChild("connectionStatus").equalTo("connected");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Connections>()
                .setQuery(query, Connections.class)
                .build();

        FirebaseRecyclerAdapter<Connections,ConnectionsViewHolder> adapter = new FirebaseRecyclerAdapter<Connections, ConnectionsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ConnectionsViewHolder connectionsViewHolder, int i, @NonNull Connections connections)
            {
                /* getting root user id */
                final String userID = getRef(i).getKey();

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
                                if(!TextUtils.isEmpty(name))
                                {
                                    connectionsViewHolder.userName.setText(name);
                                }
                            }

                            if(dataSnapshot.hasChild("userimage"))
                            {
                                String image = dataSnapshot.child("userimage").getValue().toString();
                                if(!TextUtils.isEmpty(image))
                                {
                                    Picasso.with(ConnectionsActivity.this).load(image).placeholder(R.drawable.default_user_image).into(connectionsViewHolder.userImage);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

                connectionsViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        UserSendToProfilePage(userID);
                    }
                });
            }

            @NonNull
            @Override
            public ConnectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout,parent,false);
                ConnectionsViewHolder connectionsViewHolder = new ConnectionsViewHolder(view);
                return connectionsViewHolder;
            }
        };
        connectionsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ConnectionsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        CircleImageView userImage;

        public ConnectionsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userImage = (CircleImageView) itemView.findViewById(R.id.connection_userimage);
            userName = (TextView) itemView.findViewById(R.id.connection_username);
        }
    }

    private void UserSendToProfilePage(String userID)
    {
        Intent mainIntent = new Intent(ConnectionsActivity.this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
        mainIntent.putExtra("intentUserID", userID);
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
