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

public class ServiceManagerActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference servicesDatabaseReference, ordersDatabaseReference, userDatabaseReference;

    private RecyclerView serviceRequestsList;

    String currentUserID, intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_manager);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Services Manager");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("ViewService"))
        {
            //intentServiceID = intent.getExtras().getString("intentServiceID");
        }


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ordersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Orders");
        servicesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");

        serviceRequestsList = (RecyclerView) findViewById(R.id.service_order_list);
        serviceRequestsList.setNestedScrollingEnabled(false);
        serviceRequestsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define requests order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        serviceRequestsList.setLayoutManager(linearLayoutManager);

        LoadServiceOrders();

    }


    private void LoadServiceOrders()
    {
        Query query = ordersDatabaseReference.orderByChild("seller").equalTo(currentUserID);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Orders>()
                .setQuery(query, Orders.class)
                .build();

        FirebaseRecyclerAdapter<Orders,ServiceOrdersViewHolder> adapter = new FirebaseRecyclerAdapter<Orders, ServiceOrdersViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceOrdersViewHolder serviceOrdersViewHolder, int i, @NonNull final Orders orders)
            {
                /* getting order id */
                final String orderID = getRef(i).getKey();

                final String orderStatus = orders.getOrderStatus();
                if(orderStatus.equals("pending") || orderStatus.equals("accepted") || orderStatus.equals("rejected") ||
                        orderStatus.equals("finalized")  || orderStatus.equals("finalizedWithRatings") || orderStatus.equals("cancelled"))
                {
                    if(orderStatus.equals("pending")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Pending"); }
                    if(orderStatus.equals("accepted")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Accepted by You"); }
                    if(orderStatus.equals("rejected")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Rejected by You"); }
                    if(orderStatus.equals("finalized")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Finalized"); }
                    if(orderStatus.equals("finalizedWithRatings")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Finalized and, Rated by Buyer"); }
                    if(orderStatus.equals("cancelled")) { serviceOrdersViewHolder.orderStatus.setText("Order Status : Cancelled by Buyer"); }

                    /* getting date */
                    String date = orders.getDate();
                    serviceOrdersViewHolder.orderDate.setText("Order Time : "+date);

                    /* getting time */
                    String time = orders.getTime();
                    serviceOrdersViewHolder.orderTime.setText(" • "+time);


                    /* getting buyer details */
                    String buyer = orders.getBuyer();
                    if(!buyer.isEmpty())
                    {
                        userDatabaseReference.child(buyer).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("userimage"))
                                    {
                                        String userimage = dataSnapshot.child("userimage").getValue().toString();
                                        if(!userimage.isEmpty())
                                        {
                                            Picasso.with(getApplication()).load(userimage).placeholder(R.drawable.default_user_image).into(serviceOrdersViewHolder.userImage);
                                        }
                                    }

                                    if(dataSnapshot.hasChild("username"))
                                    {
                                        String username = dataSnapshot.child("username").getValue().toString();
                                        if(!username.isEmpty())
                                        {
                                            serviceOrdersViewHolder.userName.setText(username);
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


                    /* getting service details */
                    String serviceID = orders.getServiceID();
                    if(!serviceID.isEmpty())
                    {
                        servicesDatabaseReference.child(serviceID).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("servicetitle"))
                                    {
                                        String serviceTitle = dataSnapshot.child("servicetitle").getValue().toString();
                                        if(!serviceTitle.isEmpty())
                                        {
                                            serviceOrdersViewHolder.serviceTitle.setText("Service : "+serviceTitle);
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
                }
                else
                {
                    /* Removed orders removing from recycle view */
                    serviceOrdersViewHolder.itemView.setVisibility(View.GONE);
                    serviceOrdersViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }


                if(orderStatus.equals("pending"))
                {
                    serviceOrdersViewHolder.acceptBtn.setText("Accept");
                    serviceOrdersViewHolder.acceptBtn.setEnabled(true);
                    serviceOrdersViewHolder.acceptBtn.setAlpha(1f);
                    serviceOrdersViewHolder.acceptBtn.setTextColor(getResources().getColor(R.color.colorPrimary));

                    serviceOrdersViewHolder.rejectBtn.setText("Reject");
                    serviceOrdersViewHolder.rejectBtn.setEnabled(true);
                    serviceOrdersViewHolder.rejectBtn.setAlpha(1f);
                    serviceOrdersViewHolder.rejectBtn.setTextColor(getResources().getColor(R.color.WarningTextColor));

                    serviceOrdersViewHolder.finalizeBtn.setText("Finalize the Order");
                    serviceOrdersViewHolder.finalizeBtn.setEnabled(false);
                    serviceOrdersViewHolder.finalizeBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.finalizeBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }

                if(orderStatus.equals("accepted"))
                {
                    serviceOrdersViewHolder.acceptBtn.setText("Accepted");
                    serviceOrdersViewHolder.acceptBtn.setEnabled(false);
                    serviceOrdersViewHolder.acceptBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.acceptBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.rejectBtn.setText("Reject");
                    serviceOrdersViewHolder.rejectBtn.setEnabled(false);
                    serviceOrdersViewHolder.rejectBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.rejectBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.finalizeBtn.setText("Finalize the Order");
                    serviceOrdersViewHolder.finalizeBtn.setEnabled(true);
                    serviceOrdersViewHolder.finalizeBtn.setAlpha(1f);
                    serviceOrdersViewHolder.finalizeBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                if(orderStatus.equals("rejected"))
                {
                    serviceOrdersViewHolder.acceptBtn.setText("Accept");
                    serviceOrdersViewHolder.acceptBtn.setEnabled(false);
                    serviceOrdersViewHolder.acceptBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.acceptBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.rejectBtn.setText("Rejected");
                    serviceOrdersViewHolder.rejectBtn.setEnabled(false);
                    serviceOrdersViewHolder.rejectBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.rejectBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.finalizeBtn.setText("Finalize the Order");
                    serviceOrdersViewHolder.finalizeBtn.setEnabled(false);
                    serviceOrdersViewHolder.finalizeBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.finalizeBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }


                if(orderStatus.equals("finalized") || orderStatus.equals("finalizedWithRatings") || orderStatus.equals("cancelled"))
                {
                    serviceOrdersViewHolder.acceptBtn.setText("Accepted");
                    serviceOrdersViewHolder.acceptBtn.setEnabled(false);
                    serviceOrdersViewHolder.acceptBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.acceptBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.rejectBtn.setText("Reject");
                    serviceOrdersViewHolder.rejectBtn.setEnabled(false);
                    serviceOrdersViewHolder.rejectBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.rejectBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    serviceOrdersViewHolder.finalizeBtn.setText("Finalized the Order");
                    serviceOrdersViewHolder.finalizeBtn.setEnabled(false);
                    serviceOrdersViewHolder.finalizeBtn.setAlpha(.5f);
                    serviceOrdersViewHolder.finalizeBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }


                serviceOrdersViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        HashMap orderMap = new HashMap();
                        orderMap.put("orderStatus", "accepted");
                        ordersDatabaseReference.child(orderID).updateChildren(orderMap);
                    }
                });


                serviceOrdersViewHolder.rejectBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(orderStatus.equals("pending"))
                        {
                            HashMap orderMap = new HashMap();
                            orderMap.put("orderStatus", "rejected");
                            ordersDatabaseReference.child(orderID).updateChildren(orderMap);
                        }
                    }
                });

                serviceOrdersViewHolder.finalizeBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        HashMap orderMap = new HashMap();
                        orderMap.put("orderStatus", "finalized");
                        ordersDatabaseReference.child(orderID).updateChildren(orderMap);
                    }
                });

                serviceOrdersViewHolder.userImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent mainIntent = new Intent(ServiceManagerActivity.this, MainActivity.class);
                        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                        mainIntent.putExtra("intentUserID", orders.getBuyer());
                        startActivity(mainIntent);
                    }
                });

                serviceOrdersViewHolder.userName.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent mainIntent = new Intent(ServiceManagerActivity.this, MainActivity.class);
                        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                        mainIntent.putExtra("intentUserID", orders.getBuyer());
                        startActivity(mainIntent);
                    }
                });

                serviceOrdersViewHolder.serviceTitle.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent viewServiceIntent = new Intent(ServiceManagerActivity.this, ViewServiceActivity.class);
                        viewServiceIntent.putExtra("intentPurpose", "ViewService");
                        viewServiceIntent.putExtra("intentServiceID", orders.getServiceID());
                        startActivity(viewServiceIntent);
                    }
                });

            }

            @NonNull
            @Override
            public ServiceOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_orders_layout,parent,false);
                ServiceOrdersViewHolder serviceOrdersViewHolder = new ServiceOrdersViewHolder(view);
                return serviceOrdersViewHolder;
            }
        };

        serviceRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ServiceOrdersViewHolder extends RecyclerView.ViewHolder
    {
        TextView serviceTitle, userName, orderDate, orderTime, orderStatus;
        Button acceptBtn, rejectBtn, finalizeBtn;
        CircleImageView userImage;

        public ServiceOrdersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            serviceTitle = itemView.findViewById(R.id.service_order_service_title);
            orderDate = itemView.findViewById(R.id.service_order_order_date);
            orderTime = itemView.findViewById(R.id.service_order_order_time);
            orderStatus = itemView.findViewById(R.id.service_order_order_status);
            userImage = (CircleImageView) itemView.findViewById(R.id.service_order_user_image);
            userName = (TextView) itemView.findViewById(R.id.service_order_username);
            acceptBtn = (Button) itemView.findViewById(R.id.service_order_accept_button);
            rejectBtn = (Button) itemView.findViewById(R.id.service_order_reject_button);
            finalizeBtn = (Button) itemView.findViewById(R.id.service_order_finalize_button);
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
