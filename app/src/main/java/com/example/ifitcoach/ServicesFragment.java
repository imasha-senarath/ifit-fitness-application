package com.example.ifitcoach;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


public class ServicesFragment extends Fragment
{
    private View servicesView;

    private EditText searchBar;

    private RecyclerView serviceList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference serviceDatabaseReference, userDatabaseReference;

    private FloatingActionButton createServiceBtn;


    String currentUserID;

    public ServicesFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        servicesView =  inflater.inflate(R.layout.fragment_services, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        serviceDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        serviceList = (RecyclerView) servicesView.findViewById(R.id.services_list);
        serviceList.setNestedScrollingEnabled(false);
        serviceList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        /* define service order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        serviceList.setLayoutManager(linearLayoutManager);


        searchBar = (EditText) servicesView.findViewById(R.id.services_search_bar);
        searchBar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent findIntent = new Intent(getActivity(), FindActivity.class);
                findIntent.putExtra("intentPurpose", "FindServices");
                startActivity(findIntent);
            }
        });

        createServiceBtn = servicesView.findViewById(R.id.service_fragment_create_service_button);
        createServiceBtn.setVisibility(View.GONE);
        createServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserSendToCreateServicePage();
            }
        });

        RetrieveUserDetails();

        return servicesView;
    }



    private void RetrieveUserDetails()
    {
        userDatabaseReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("useraccounttype"))
                    {
                        String userAccountType = dataSnapshot.child("useraccounttype").getValue().toString();
                        if(userAccountType.equals("SELL A SERVICE"))
                        {
                            createServiceBtn.setVisibility(View.VISIBLE);
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


    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserServices>()
                .setQuery(serviceDatabaseReference, UserServices.class)
                .build();

        FirebaseRecyclerAdapter<UserServices,ServiceViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserServices, ServiceViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolder serviceViewHolder, int i, @NonNull UserServices userServices)
            {
                /* get service ID */
                final String serviceID = getRef(i).getKey();

                serviceViewHolder.serviceTitle.setText("Service : "+userServices.getServicetitle());
                serviceViewHolder.serviceDate.setText(userServices.servicedate);
                serviceViewHolder.serviceTime.setText(userServices.servicetime);
                Picasso.with(getContext()).load(userServices.getServicefirstimage()).into(serviceViewHolder.serviceFirstImage);
                serviceViewHolder.serviceSummaryDescription.setText(userServices.servicesummarydescription);

                final String serviceUserID = userServices.getUserid();
                userDatabaseReference.child(serviceUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            Picasso.with(getContext()).load(R.drawable.default_user_image).into(serviceViewHolder.serviceOwnerImage);
                            serviceViewHolder.serviceOwnerPosition.setVisibility(View.GONE);

                            if (dataSnapshot.hasChild("username"))
                            {
                                String name = dataSnapshot.child("username").getValue().toString();
                                serviceViewHolder.serviceOwnerUsername.setText(name);
                            }

                            if(dataSnapshot.hasChild("userimage"))
                            {
                                String image = dataSnapshot.child("userimage").getValue().toString();
                                Picasso.with(getContext()).load(image).placeholder(R.drawable.default_user_image).into(serviceViewHolder.serviceOwnerImage);
                            }

                            if (dataSnapshot.hasChild("userposition"))
                            {
                                String position = dataSnapshot.child("userposition").getValue().toString();
                                serviceViewHolder.serviceOwnerPosition.setText(" • "+position);
                                serviceViewHolder.serviceOwnerPosition.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


                serviceViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent viewServiceIntent = new Intent(getActivity(), ViewServiceActivity.class);
                        viewServiceIntent.putExtra("intentPurpose", "ViewService");
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
                ServiceViewHolder viewHolder = new ServiceViewHolder(view);
                return viewHolder;
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
        Intent createServiceIntent = new Intent(getContext(), CreateServiceActivity.class);
        startActivity(createServiceIntent);
    }
}
