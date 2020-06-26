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
import android.widget.Toast;

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

public class RequestActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference connectionsDatabaseReference, userDatabaseReference;

    private RecyclerView requestsList;

    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        /* Adding tool bar & title to add request activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        connectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections");

        requestsList = (RecyclerView) findViewById(R.id.requests_list);
        requestsList.setNestedScrollingEnabled(false);
        requestsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define requests order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        requestsList.setLayoutManager(linearLayoutManager);

        LoadRequests();
    }



    private void LoadRequests()
    {
        Query query = connectionsDatabaseReference.child(currentUserID).orderByChild("connectionStatus").equalTo("respond");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Connections>()
                .setQuery(query, Connections.class)
                .build();

        FirebaseRecyclerAdapter<Connections,RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Connections, RequestsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder requestsViewHolder, int i, @NonNull Connections connections)
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
                                    requestsViewHolder.userName.setText(name);
                                }
                            }

                            if(dataSnapshot.hasChild("userimage"))
                            {
                                String image = dataSnapshot.child("userimage").getValue().toString();
                                if(!TextUtils.isEmpty(image))
                                {
                                    Picasso.with(RequestActivity.this).load(image).placeholder(R.drawable.default_user_image).into(requestsViewHolder.userImage);
                                }

                            }
                        }

                        requestsViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                HashMap connectionDiaryMap1 = new HashMap();
                                connectionDiaryMap1.put("connectionStatus", "connected");
                                connectionsDatabaseReference.child(userID).child(currentUserID).updateChildren(connectionDiaryMap1);

                                HashMap connectionDiaryMap2 = new HashMap();
                                connectionDiaryMap2.put("connectionStatus", "connected");
                                connectionsDatabaseReference.child(currentUserID).child(userID).updateChildren(connectionDiaryMap2);
                            }
                        });

                        requestsViewHolder.rejectBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                connectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(currentUserID).child(userID);
                                connectionsDatabaseReference.removeValue();
                                connectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(userID).child(currentUserID);
                                connectionsDatabaseReference.removeValue();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

                requestsViewHolder.userName.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        UserSendToProfilePage(userID);
                    }
                });

                requestsViewHolder.userImage.setOnClickListener(new View.OnClickListener()
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
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connection_respond_layout,parent,false);
                RequestsViewHolder requestsViewHolder = new RequestsViewHolder(view);
                return requestsViewHolder;
            }
        };
        requestsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        Button acceptBtn, rejectBtn;
        CircleImageView userImage;

        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userImage = (CircleImageView) itemView.findViewById(R.id.connection_respond_userimage);
            userName = (TextView) itemView.findViewById(R.id.connection_respond_username);
            acceptBtn = (Button) itemView.findViewById(R.id.connection_respond_accept_button);
            rejectBtn = (Button) itemView.findViewById(R.id.connection_respond_reject_button);
        }
    }


    private void UserSendToProfilePage(String userID)
    {
        Intent mainIntent = new Intent(RequestActivity.this, MainActivity.class);
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
