package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewServiceActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference serviceDatabaseReference, deleteServiceDatabaseReference, userDatabaseReference,
            orderDatabaseReference, serviceRatingsDatabaseReference;

    private ImageView ServiceSettingsBtn;
    private TextView ServiceTitle, ServiceDate, ServiceTime, ServiceSummaryDescription, ServiceFullDescription, ServiceOwnerUsername,
            ServiceOwnerPosition,  ServiceRatingValue, ServiceOrderCount;
    private CircleImageView ServiceOwnerImage;
    private ImageView ServiceFirstImage, ServiceSecondImage, ServiceThirdImage;
    private RatingBar ServiceRatingBar;


    private Button ServiceContactBtn, ServiceOrderBtn;

    private String strServiceTitle, strServiceDate, strServiceTime, strServiceSummaryDescription, strServiceFullDescription,
            strServiceOwnerUserID, strServiceOwnerUsername, strServiceOwnerPosition, strServiceOwnerImage,
            strServiceFirstImage, strServiceSecondImage, strServiceThirdImage;

    String currentUserID, intentFrom, intentServiceID;

    private String orderCount="0.0", ratingValue="0.0";
    private long tempOrderCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_service);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View Service");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        ServiceSettingsBtn = findViewById(R.id.view_service_service_settings);
        ServiceSettingsBtn.setVisibility(View.GONE);
        ServiceTitle = findViewById(R.id.view_service_service_title);
        ServiceDate = findViewById(R.id.view_service_service_date);
        ServiceTime = findViewById(R.id.view_service_service_time);
        ServiceSummaryDescription = findViewById(R.id.view_service_service_summary_description);
        ServiceFullDescription = findViewById(R.id.view_service_service_full_description);
        ServiceFirstImage = findViewById(R.id.view_service_service_first_image);
        ServiceSecondImage = findViewById(R.id.view_service_service_second_image);
        ServiceThirdImage = findViewById(R.id.view_service_service_third_image);
        ServiceOwnerUsername = findViewById(R.id.view_service_service_owner_username);
        ServiceOwnerPosition = findViewById(R.id.view_service_service_owner_position);
        ServiceOwnerImage = findViewById(R.id.view_service_service_owner_image);
        ServiceRatingValue = findViewById(R.id.view_service_rating_value);
        ServiceOrderCount = findViewById(R.id.view_service_order_count);
        ServiceRatingBar = findViewById(R.id.view_service_rating_bar);


        ServiceOrderBtn = findViewById(R.id.view_service_order_button);
        ServiceContactBtn = findViewById(R.id.view_service_contact_button);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentPurpose");
        if(intentFrom.equals("ViewService") || intentFrom.equals("MyServices") || intentFrom.equals("OrdersActivity"))
        {
            intentServiceID = intent.getExtras().getString("intentServiceID");
        }


        firebaseAuth= FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        serviceDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services").child(intentServiceID);
        orderDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Orders");
        serviceRatingsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ServiceRatings");


        ServiceOrderBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OrderButtonClickActions();
            }
        });


        ServiceSettingsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenServiceMenu();
            }
        });

        ServiceContactBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToChatPage();
            }
        });

        ServiceOwnerImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToProfilePage();
            }
        });

        ServiceOwnerUsername.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToProfilePage();
            }
        });

        ServiceOwnerPosition.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToProfilePage();
            }
        });


        RetrieveAndSetServiceDetails();

    }


    private void OpenServiceMenu()
    {
        final Dialog serviceMenu = new Dialog(this);
        serviceMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        serviceMenu.setContentView(R.layout.service_menu_layout);
        serviceMenu.setTitle("Service Menu Window");
        serviceMenu.show();
        Window serviceMenuWindow = serviceMenu.getWindow();
        serviceMenuWindow.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout requestsBtn = (RelativeLayout) serviceMenu.findViewById(R.id.service_menu_service_requests_button);
        requestsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent serviceManagerIntent = new Intent(ViewServiceActivity.this, ServiceManagerActivity.class);
                serviceManagerIntent.putExtra("intentFrom", "ViewService");
                serviceManagerIntent.putExtra("intentServiceID", intentServiceID);
                startActivity(serviceManagerIntent);
                serviceMenu.dismiss();
            }
        });

        RelativeLayout deleteBtn = (RelativeLayout) serviceMenu.findViewById(R.id.service_menu_service_delete_button);
        deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                serviceMenu.dismiss();
                OpenServiceDeleteConfirmDialog();
            }
        });

    }


    private void OpenServiceDeleteConfirmDialog()
    {
        final Dialog serviceDeleteConfirmDialog = new Dialog(this);
        serviceDeleteConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        serviceDeleteConfirmDialog.setContentView(R.layout.confirm_layout);
        serviceDeleteConfirmDialog.setTitle("Cancel Order Confirm Dialog");
        serviceDeleteConfirmDialog.show();

        TextView title = (TextView)serviceDeleteConfirmDialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Delete Service");

        TextView description = (TextView)serviceDeleteConfirmDialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to delete the service?");

        Button noBtn = (Button)serviceDeleteConfirmDialog.findViewById(R.id.confirm_dialog_no_button);
        noBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                serviceDeleteConfirmDialog.cancel();
            }
        });

        Button yesBtn = (Button)serviceDeleteConfirmDialog.findViewById(R.id.confirm_dialog_yes_button);
        yesBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteServiceDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services").child(intentServiceID);
                deleteServiceDatabaseReference.removeValue();
                Toast.makeText(ViewServiceActivity.this, "Service deleted", Toast.LENGTH_SHORT).show();
                serviceDeleteConfirmDialog.cancel();
                onBackPressed();
            }
        });
    }


    private void OrderButtonClickActions()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
        String date = simpleDateFormat1.format(calendar.getTime());
        SimpleDateFormat simpleTimeFormat1 = new SimpleDateFormat("HHmmss");
        String time = simpleTimeFormat1.format(calendar.getTime());

        String randomOrderID = date + time + currentUserID;


        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM yyyy");
        String currentDate = simpleDateFormat2.format(calendar.getTime());

        SimpleDateFormat simpleTimeFormat2 = new SimpleDateFormat("hh:mm aa");
        String currentTime = simpleTimeFormat2.format(calendar.getTime());

        HashMap orderMap = new HashMap();
        orderMap.put("date", currentDate);
        orderMap.put("time", currentTime);
        orderMap.put("orderStatus", "pending");
        orderMap.put("buyer", currentUserID);
        orderMap.put("seller", strServiceOwnerUserID);
        orderMap.put("serviceID", intentServiceID);
        orderDatabaseReference.child(randomOrderID).updateChildren(orderMap);
        Toast.makeText(this, "Service added to Orders", Toast.LENGTH_SHORT).show();
    }




    private void RetrieveAndSetServiceDetails()
    {
        serviceDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("servicetitle"))
                    {
                        strServiceTitle = dataSnapshot.child("servicetitle").getValue().toString();
                        if(!strServiceTitle.isEmpty())
                        {
                            ServiceTitle.setText(strServiceTitle);
                        }
                    }

                    if(dataSnapshot.hasChild("servicedate"))
                    {
                        strServiceDate = dataSnapshot.child("servicedate").getValue().toString();
                        if(!strServiceDate.isEmpty())
                        {
                            ServiceDate.setText(strServiceDate);
                        }
                    }

                    if(dataSnapshot.hasChild("servicetime"))
                    {
                        strServiceTime = dataSnapshot.child("servicetime").getValue().toString();
                        if(!strServiceTime.isEmpty())
                        {
                            ServiceTime.setText(strServiceTime);
                        }
                    }

                    if(dataSnapshot.hasChild("servicesummarydescription"))
                    {
                        strServiceSummaryDescription = dataSnapshot.child("servicesummarydescription").getValue().toString();
                        if(!strServiceSummaryDescription.isEmpty())
                        {
                            ServiceSummaryDescription.setText(strServiceSummaryDescription);
                        }
                    }

                    if(dataSnapshot.hasChild("servicefulldescription"))
                    {
                        strServiceFullDescription = dataSnapshot.child("servicefulldescription").getValue().toString();
                        if(!strServiceFullDescription.isEmpty())
                        {
                            ServiceFullDescription.setText(strServiceFullDescription);
                        }
                    }

                    if(dataSnapshot.hasChild("servicefirstimage"))
                    {
                        strServiceFirstImage = dataSnapshot.child("servicefirstimage").getValue().toString();
                        if(!strServiceFirstImage.isEmpty())
                        {
                            Picasso.with(getApplication()).load(strServiceFirstImage).placeholder(R.drawable.default_image).into(ServiceFirstImage);
                        }
                    }

                    if(dataSnapshot.hasChild("servicesecondimage"))
                    {
                        strServiceSecondImage = dataSnapshot.child("servicesecondimage").getValue().toString();
                        if(!strServiceSecondImage.isEmpty())
                        {
                            ServiceSecondImage.setVisibility(View.VISIBLE);
                            Picasso.with(getApplication()).load(strServiceSecondImage).placeholder(R.drawable.default_image).into(ServiceSecondImage);
                        }
                    }


                    if(dataSnapshot.hasChild("servicethirdimage"))
                    {
                        strServiceThirdImage = dataSnapshot.child("servicethirdimage").getValue().toString();
                        if(!strServiceThirdImage.isEmpty())
                        {
                            ServiceThirdImage.setVisibility(View.VISIBLE);
                            Picasso.with(getApplication()).load(strServiceThirdImage).placeholder(R.drawable.default_image).into(ServiceThirdImage);
                        }
                    }


                    if(dataSnapshot.hasChild("userid"))
                    {
                        strServiceOwnerUserID = dataSnapshot.child("userid").getValue().toString();
                        if(strServiceOwnerUserID.equals(currentUserID))
                        {
                            ServiceContactBtn.setVisibility(View.GONE);
                            ServiceOrderBtn.setVisibility(View.GONE);
                            ServiceSettingsBtn.setVisibility(View.VISIBLE);
                        }

                        if(!strServiceOwnerUserID.isEmpty())
                        {
                            userDatabaseReference.child(strServiceOwnerUserID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("username"))
                                        {
                                            strServiceOwnerUsername = dataSnapshot.child("username").getValue().toString();
                                            if(!strServiceOwnerUsername.isEmpty())
                                            {
                                                ServiceOwnerUsername.setText(strServiceOwnerUsername);
                                            }
                                        }

                                        if(dataSnapshot.hasChild("userimage"))
                                        {
                                            strServiceOwnerImage = dataSnapshot.child("userimage").getValue().toString();
                                            if(!strServiceOwnerImage.isEmpty())
                                            {
                                                Picasso.with(getApplication()).load(strServiceOwnerImage).placeholder(R.drawable.profile_image_placeholder).into(ServiceOwnerImage);
                                            }
                                        }

                                        if(dataSnapshot.hasChild("userposition"))
                                        {
                                            strServiceOwnerPosition = dataSnapshot.child("userposition").getValue().toString();
                                            if(!strServiceOwnerPosition.isEmpty())
                                            {
                                                ServiceOwnerPosition.setText(" • "+strServiceOwnerPosition);
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

                    serviceRatingsDatabaseReference.orderByChild("serviceID").equalTo(intentServiceID).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                tempOrderCount = dataSnapshot.getChildrenCount();
                                orderCount = String.valueOf(tempOrderCount);
                                ServiceOrderCount.setText(orderCount+" Orders");

                                for(final DataSnapshot ds : dataSnapshot.getChildren())
                                {
                                    String orderID = ds.getKey();
                                    serviceRatingsDatabaseReference.child(orderID).addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.exists())
                                            {
                                                if(dataSnapshot.hasChild("ratingValue"))
                                                {
                                                    String value = dataSnapshot.child("ratingValue").getValue().toString();
                                                    ratingValue = String.valueOf(Float.parseFloat(ratingValue) + Float.parseFloat(value));
                                                }

                                                tempOrderCount = tempOrderCount - 1;

                                                /* checking whether  finished the calculation or not */
                                                if(tempOrderCount == 0)
                                                {
                                                    ratingValue = String.valueOf(Float.parseFloat(ratingValue) / Float.parseFloat(orderCount));
                                                    ratingValue = ratingValue.replace(",","");

                                                    ServiceRatingValue.setText(String.format("%.1f", Double.parseDouble(ratingValue)));
                                                    ServiceRatingBar.setRating(Float.parseFloat(ratingValue));

                                                    ratingValue = "0";
                                                    orderCount="0";
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void UserSendToChatPage()
    {
        Intent viewMessagesIntent = new Intent(ViewServiceActivity.this, ViewMessagesActivity.class);
        viewMessagesIntent.putExtra("intentUserID", strServiceOwnerUserID);
        startActivity(viewMessagesIntent);
    }


    private void UserSendToProfilePage()
    {
        Intent mainIntent = new Intent(ViewServiceActivity.this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
        mainIntent.putExtra("intentUserID", strServiceOwnerUserID);
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
