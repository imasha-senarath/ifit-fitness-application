package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.io.DataOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, serviceDatabaseReference;

    private Spinner searchType;
    private EditText searchBar;

    private RecyclerView searchResultList;

    String intentPurpose, strSearchType;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);


        /* Adding tool bar & title to find activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentPurpose = intent.getExtras().getString("intentPurpose");


        firebaseAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        serviceDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");

        searchBar = (EditText) findViewById(R.id.find_search_bar);
        searchBar.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);

        searchType = findViewById(R.id.find_search_type);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.searchType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchType.setAdapter(adapter);
        searchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                strSearchType = parent.getItemAtPosition(position).toString();
                if(strSearchType.equals("People"))
                {
                    searchBar.setText("");
                    searchBar.setHint("Search for a People");
                }

                if(strSearchType.equals("Service"))
                {
                    searchBar.setText("");
                    searchBar.setHint("Search for a Service");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        if(intentPurpose.equals("FindUsers"))
        {
            strSearchType = "People";
            searchType.setSelection(0);
            searchBar.setHint("Search for a People");
        }
        if(intentPurpose.equals("FindServices"))
        {
            strSearchType = "Service";
            searchType.setSelection(1);
            searchBar.setHint("Search for a Service");
        }


        searchResultList = (RecyclerView)findViewById(R.id.search_list);
        searchResultList.setNestedScrollingEnabled(false);
        searchResultList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define post order*/
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        searchResultList.setLayoutManager(linearLayoutManager);


        /* search bar actions */
        searchBar.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String searchBarInput = searchBar.getText().toString().toLowerCase();
                if(searchBarInput.isEmpty())
                {
                    DisplayUserSearchResult(searchBarInput);
                }
                else
                {
                    if(strSearchType.equals("People"))
                    {
                        DisplayUserSearchResult(searchBarInput);
                    }

                    if(strSearchType.equals("Service"))
                    {
                        DisplayServiceSearchResult(searchBarInput);
                    }

                }
            }
        });

    }

    private void DisplayServiceSearchResult(final String searchBarInput)
    {
        Query searchServiceQuery = serviceDatabaseReference.orderByChild("servicesearchkeyword").startAt(searchBarInput).endAt(searchBarInput + "\uf8ff");

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserServices>()
                        .setQuery(searchServiceQuery, UserServices.class)
                        .build();

        FirebaseRecyclerAdapter<UserServices, ServiceViewHolder> adapter = new FirebaseRecyclerAdapter<UserServices, ServiceViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolder serviceViewHolder, int i, @NonNull UserServices userServices)
            {
                final String serviceID = getRef(i).getKey();

                if(!searchBarInput.isEmpty())
                {
                    serviceViewHolder.serviceTitle.setText("Service : "+userServices.getServicetitle());
                    serviceViewHolder.serviceDate.setText(userServices.servicedate);
                    serviceViewHolder.serviceTime.setText(userServices.servicetime);
                    Picasso.with(getApplication()).load(userServices.getServicefirstimage()).into(serviceViewHolder.serviceFirstImage);
                    serviceViewHolder.serviceSummaryDescription.setText(userServices.servicesummarydescription);

                    userDatabaseReference.child(userServices.getUserid()).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.hasChild("username"))
                                {
                                    String username = dataSnapshot.child("username").getValue().toString();
                                    serviceViewHolder.serviceOwnerUsername.setText(username);
                                }

                                if(dataSnapshot.hasChild("userimage"))
                                {
                                    String userImage = dataSnapshot.child("userimage").getValue().toString();
                                    Picasso.with(getApplication()).load(userImage).placeholder(R.drawable.profile_image_placeholder).into(serviceViewHolder.serviceOwnerImage);
                                }

                                if(dataSnapshot.hasChild("userposition"))
                                {
                                    String position = dataSnapshot.child("userposition").getValue().toString();
                                    serviceViewHolder.serviceOwnerPosition.setText(" • "+position);

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    serviceViewHolder.itemView.setVisibility(View.GONE);
                }

                serviceViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent viewServiceIntent = new Intent(FindActivity.this, ViewServiceActivity.class);
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
                ServiceViewHolder serviceViewHolder = new ServiceViewHolder(view);
                return serviceViewHolder;
            }
        };

        searchResultList.setAdapter(adapter);
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




    private void DisplayUserSearchResult(final String searchBarInput)
    {
        Query searchUserQuery = userDatabaseReference.orderByChild("usersearchkeyword").startAt(searchBarInput).endAt(searchBarInput + "\uf8ff");

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(searchUserQuery, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users,UserViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull Users users)
            {
                final String userID = getRef(i).getKey();

                if(!searchBarInput.isEmpty())
                {
                    String username = users.getUsername();
                    userViewHolder.userName.setText(username);

                    String userImage = users.getUserimage();
                    if(!TextUtils.isEmpty(userImage))
                    {
                        Picasso.with(FindActivity.this).load(userImage).placeholder(R.drawable.default_user_image).into(userViewHolder.userImage);
                    }
                }
                else
                {
                    userViewHolder.itemView.setVisibility(View.GONE);
                }

                userViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent = new Intent(FindActivity.this, MainActivity.class);
                        profileIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                        profileIntent.putExtra("intentUserID", userID);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
                UserViewHolder userViewHolder = new UserViewHolder(view);
                return userViewHolder;
            }
        };

        searchResultList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        CircleImageView userImage;

        public UserViewHolder(@NonNull View itemView)
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
