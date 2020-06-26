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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ServiceActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, serviceDatabaseReference;

    private String username, userPosition, userImage;

    String currentUserID, intentFrom, intentUserID;

    private FloatingActionButton createServicesButton;

    private RecyclerView serviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);


        /* Adding tool bar & title to add service activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Services");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("ViewAnotherUserProfile") || intentFrom.equals("MyProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        serviceDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");

        serviceList = (RecyclerView)findViewById(R.id.services_services_list);
        serviceList.setNestedScrollingEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define Service order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        serviceList.setLayoutManager(linearLayoutManager);


        createServicesButton = (FloatingActionButton) findViewById(R.id.service_create_service_button);
        if(!currentUserID.equals(intentUserID))
        {
            createServicesButton.setVisibility(View.GONE);
        }
        createServicesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToCreateServicePage();
            }
        });

        DisplayUserAllServices();
        RetrieveUserDetails();
    }


    private void RetrieveUserDetails()
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
                        username = dataSnapshot.child("username").getValue().toString();
                        if(intentFrom.equals("ViewAnotherUserProfile"))
                        {
                            if(!TextUtils.isEmpty(username))
                            {
                                /* getting first name */
                                String arr[] = username.split(" ", 2);
                                getSupportActionBar().setTitle(arr[0]+"'s"+" Services");
                            }
                        }
                    }

                    if(dataSnapshot.hasChild("userposition"))
                    {
                        userPosition = dataSnapshot.child("userposition").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userimage"))
                    {
                        userImage = dataSnapshot.child("userimage").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void DisplayUserAllServices()
    {
        Query currentUserServices = serviceDatabaseReference.orderByChild("userid").equalTo(intentUserID);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserServices>()
                        .setQuery(currentUserServices, UserServices.class)
                        .build();

        FirebaseRecyclerAdapter<UserServices, ServiceViewHolder> adapter = new FirebaseRecyclerAdapter<UserServices, ServiceViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolder serviceViewHolder, int i, @NonNull UserServices userServices)
            {
                final String serviceID = getRef(i).getKey();

                serviceViewHolder.serviceTitle.setText("Service : "+userServices.getServicetitle());
                serviceViewHolder.serviceDate.setText(userServices.servicedate);
                serviceViewHolder.serviceTime.setText(userServices.servicetime);
                Picasso.with(getApplication()).load(userServices.getServicefirstimage()).into(serviceViewHolder.serviceFirstImage);
                serviceViewHolder.serviceSummaryDescription.setText(userServices.servicesummarydescription);

                if(!username.isEmpty())
                {
                    serviceViewHolder.serviceOwnerUsername.setText(username);
                }

                if(!userImage.isEmpty())
                {
                    Picasso.with(getApplication()).load(userImage).placeholder(R.drawable.profile_image_placeholder).into(serviceViewHolder.serviceOwnerImage);
                }

                if(!userPosition.isEmpty())
                {
                    serviceViewHolder.serviceOwnerPosition.setText(" • "+userPosition);
                }

                /* view service */
                serviceViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent viewServiceIntent = new Intent(ServiceActivity.this, ViewServiceActivity.class);
                        viewServiceIntent.putExtra("intentPurpose", "MyServices");
                        viewServiceIntent.putExtra("intentServiceID", serviceID);
                        startActivity(viewServiceIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_layout, parent, false);
                ServiceViewHolder serviceViewHolder = new ServiceViewHolder(view);
                return serviceViewHolder;
            }
        };

        serviceList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ServiceViewHolder extends RecyclerView.ViewHolder
    {
        TextView serviceTitle, serviceDate, serviceTime, serviceSummaryDescription, serviceOwnerUsername, serviceOwnerPosition;
        ImageView serviceFirstImage;
        CircleImageView serviceOwnerImage;

        public ServiceViewHolder(@NonNull View itemView)
        {
            super(itemView);

            serviceTitle = (TextView) itemView.findViewById(R.id.service_title);
            serviceDate = (TextView) itemView.findViewById(R.id.service_date);
            serviceTime = (TextView) itemView.findViewById(R.id.service_time);
            serviceFirstImage = (ImageView) itemView.findViewById(R.id.service_first_image);
            serviceSummaryDescription = (TextView) itemView.findViewById(R.id.service_summary_description);
            serviceOwnerImage = (CircleImageView) itemView.findViewById(R.id.service_owner_image);
            serviceOwnerUsername = (TextView) itemView.findViewById(R.id.service_owner_username);
            serviceOwnerPosition = (TextView) itemView.findViewById(R.id.service_owner_position);
        }
    }


    private void UserSendToCreateServicePage()
    {
        Intent createServiceIntent = new Intent(ServiceActivity.this, CreateServiceActivity.class);
        startActivity(createServiceIntent);
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
