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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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

public class OrdersActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference servicesDatabaseReference, ordersDatabaseReference, userDatabaseReference,
            serviceRatingDatabaseReference;

    private RecyclerView orderList;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ordersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Orders");
        servicesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");
        serviceRatingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ServiceRatings");


        orderList = (RecyclerView) findViewById(R.id.order_list);
        orderList.setNestedScrollingEnabled(false);
        orderList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define requests order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        orderList.setLayoutManager(linearLayoutManager);

        LoadAllOrders();
    }


    private void LoadAllOrders()
    {
        Query query = ordersDatabaseReference.orderByChild("buyer").equalTo(currentUserID);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Orders>()
                .setQuery(query, Orders.class)
                .build();

        FirebaseRecyclerAdapter<Orders, OrdersViewHolder> adapter = new FirebaseRecyclerAdapter<Orders, OrdersViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final OrdersViewHolder ordersViewHolder, int i, @NonNull Orders orders)
            {
                final String orderID = getRef(i).getKey();
                final String serviceID = orders.getServiceID();

                final String orderStatus = orders.getOrderStatus();
                if(orderStatus.equals("pending") || orderStatus.equals("accepted") || orderStatus.equals("rejected") || orderStatus.equals("finalized") ||
                        orderStatus.equals("finalizedWithRatings") || orderStatus.equals("cancelled"))
                {
                    ordersViewHolder.ServiceRatingContainer.setVisibility(View.GONE);

                    if(orderStatus.equals("pending")) { ordersViewHolder.orderStatus.setText("Order Status : Pending"); }
                    if(orderStatus.equals("accepted")) { ordersViewHolder.orderStatus.setText("Order Status : Accepted by Seller"); }
                    if(orderStatus.equals("rejected")) { ordersViewHolder.orderStatus.setText("Order Status : Rejected by Seller"); }

                    if(orderStatus.equals("finalized"))
                    {
                        ordersViewHolder.orderStatus.setText("Order Status :  Finalized");
                        ordersViewHolder.ServiceRatingContainer.setVisibility(View.VISIBLE);
                    }

                    if(orderStatus.equals("finalizedWithRatings")) { ordersViewHolder.orderStatus.setText("Order Status :  Finalized and, Rated by You"); }
                    if(orderStatus.equals("cancelled")) { ordersViewHolder.orderStatus.setText("Order Status : Cancelled by You"); }

                    /* getting date */
                    String date = orders.getDate();
                    ordersViewHolder.orderDate.setText("Order Time : "+date);

                    /* getting time */
                    String time = orders.getTime();
                    ordersViewHolder.orderTime.setText(" • "+time);

                    /* getting service details */
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
                                            ordersViewHolder.serviceTitle.setText("Service : "+serviceTitle);
                                        }
                                    }

                                    if(dataSnapshot.hasChild("servicefirstimage"))
                                    {
                                        String serviceImage = dataSnapshot.child("servicefirstimage").getValue().toString();
                                        if(!serviceImage.isEmpty())
                                        {
                                            Picasso.with(getApplication()).load(serviceImage).placeholder(R.drawable.default_image).into(ordersViewHolder.serviceImage);
                                        }
                                    }

                                    if(dataSnapshot.hasChild("servicesummarydescription"))
                                    {
                                        String serviceSummary = dataSnapshot.child("servicesummarydescription").getValue().toString();
                                        if(!serviceSummary.isEmpty())
                                        {
                                            ordersViewHolder.serviceSummary.setText(serviceSummary);
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
                    ordersViewHolder.itemView.setVisibility(View.GONE);
                    ordersViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

                if(orderStatus.equals("pending"))
                {
                    ordersViewHolder.viewServiceBtn.setText("View Service");
                    ordersViewHolder.viewServiceBtn.setEnabled(true);
                    ordersViewHolder.viewServiceBtn.setAlpha(1f);
                    ordersViewHolder.viewServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    ordersViewHolder.cancelOrderBtn.setText("Cancel Order");
                    ordersViewHolder.cancelOrderBtn.setEnabled(true);
                    ordersViewHolder.cancelOrderBtn.setAlpha(1f);
                    ordersViewHolder.cancelOrderBtn.setTextColor(getResources().getColor(R.color.WarningTextColor));
                }

                if(orderStatus.equals("accepted"))
                {
                    ordersViewHolder.viewServiceBtn.setText("View Service");
                    ordersViewHolder.viewServiceBtn.setEnabled(true);
                    ordersViewHolder.viewServiceBtn.setAlpha(1f);
                    ordersViewHolder.viewServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    ordersViewHolder.cancelOrderBtn.setText("Cancel Order");
                    ordersViewHolder.cancelOrderBtn.setEnabled(false);
                    ordersViewHolder.cancelOrderBtn.setAlpha(.5f);
                    ordersViewHolder.cancelOrderBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }


                if(orderStatus.equals("rejected"))
                {
                    ordersViewHolder.viewServiceBtn.setText("View Service");
                    ordersViewHolder.viewServiceBtn.setEnabled(true);
                    ordersViewHolder.viewServiceBtn.setAlpha(1f);
                    ordersViewHolder.viewServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    ordersViewHolder.cancelOrderBtn.setText("Cancel Order");
                    ordersViewHolder.cancelOrderBtn.setEnabled(false);
                    ordersViewHolder.cancelOrderBtn.setAlpha(.5f);
                    ordersViewHolder.cancelOrderBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }


                if(orderStatus.equals("finalized") || orderStatus.equals("finalizedWithRatings"))
                {
                    ordersViewHolder.viewServiceBtn.setText("View Service");
                    ordersViewHolder.viewServiceBtn.setEnabled(true);
                    ordersViewHolder.viewServiceBtn.setAlpha(1f);
                    ordersViewHolder.viewServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));


                    ordersViewHolder.cancelOrderBtn.setText("Cancel Order");
                    ordersViewHolder.cancelOrderBtn.setEnabled(false);
                    ordersViewHolder.cancelOrderBtn.setAlpha(.5f);
                    ordersViewHolder.cancelOrderBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }


                if(orderStatus.equals("cancelled"))
                {
                    ordersViewHolder.viewServiceBtn.setText("View Service");
                    ordersViewHolder.viewServiceBtn.setEnabled(true);
                    ordersViewHolder.viewServiceBtn.setAlpha(1f);
                    ordersViewHolder.viewServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));

                    ordersViewHolder.cancelOrderBtn.setText("Cancelled");
                    ordersViewHolder.cancelOrderBtn.setEnabled(false);
                    ordersViewHolder.cancelOrderBtn.setAlpha(.5f);
                    ordersViewHolder.cancelOrderBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                }



                ordersViewHolder.viewServiceBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent viewServiceIntent = new Intent(OrdersActivity.this, ViewServiceActivity.class);
                        viewServiceIntent.putExtra("intentPurpose", "OrdersActivity");
                        viewServiceIntent.putExtra("intentServiceID", serviceID);
                        startActivity(viewServiceIntent);
                    }
                });


                ordersViewHolder.cancelOrderBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(orderStatus.equals("pending"))
                        {
                            HashMap orderMap = new HashMap();
                            orderMap.put("orderStatus", "cancelled");
                            ordersDatabaseReference.child(orderID).updateChildren(orderMap);
                        }
                    }
                });

                ordersViewHolder.ServiceRatingSubmitBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        float ratingValue = ordersViewHolder.ServiceBuyerRating.getRating();
                        HashMap ratingMap = new HashMap();
                        ratingMap.put("ratingValue", String.valueOf(ratingValue));
                        ratingMap.put("serviceID", serviceID);
                        serviceRatingDatabaseReference.child(orderID).updateChildren(ratingMap);

                        HashMap orderMap = new HashMap();
                        orderMap.put("orderStatus", "finalizedWithRatings");
                        ordersDatabaseReference.child(orderID).updateChildren(orderMap);

                        Toast.makeText(OrdersActivity.this, "Rating submitted", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,parent,false);
                OrdersViewHolder ordersViewHolder = new OrdersViewHolder(view);
                return ordersViewHolder;
            }
        };

        orderList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class OrdersViewHolder extends RecyclerView.ViewHolder
    {
        TextView serviceTitle, serviceSummary, orderDate, orderTime, orderStatus;
        Button viewServiceBtn, cancelOrderBtn, ServiceRatingSubmitBtn;
        ImageView serviceImage;
        LinearLayout ServiceRatingContainer;
        RatingBar ServiceBuyerRating;

        public OrdersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            serviceTitle = itemView.findViewById(R.id.order_service_title);
            serviceImage = itemView.findViewById(R.id.order_service_first_image);
            serviceSummary = itemView.findViewById(R.id.order_service_summary_description);
            orderDate = itemView.findViewById(R.id.order_order_date);
            orderTime = itemView.findViewById(R.id.order_order_time);
            orderStatus = itemView.findViewById(R.id.order_order_status);
            viewServiceBtn = (Button) itemView.findViewById(R.id.order_service_view_button);
            cancelOrderBtn = (Button) itemView.findViewById(R.id.order_service_cancel_button);

            ServiceRatingContainer = itemView.findViewById(R.id.order_service_rating_container);
            ServiceBuyerRating = itemView.findViewById(R.id.order_service__buyer_rating_bar);
            ServiceRatingSubmitBtn = itemView.findViewById(R.id.order_service__rating_submit_button);
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
