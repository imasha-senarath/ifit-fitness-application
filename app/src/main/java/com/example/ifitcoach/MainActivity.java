package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolBar;
    private CircleImageView toolbarUserImage;
    private ImageButton toolBarProfileEditBtn;
    private ImageView toolbarLogo, toolbarLogoText;

    private BottomNavigationView bottomNavigationView;

    private String intentFrom, intentUserID;
    private String currentUserID;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();


        /* getting intent string */
        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");


        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation_bar);

        toolbarLogo = findViewById(R.id.toolbar_logo);
        toolbarLogo.setVisibility(View.VISIBLE);
        toolbarLogoText = findViewById(R.id.toolbar_logo_text);
        toolbarLogoText.setVisibility(View.VISIBLE);
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolBarProfileEditBtn = (ImageButton) findViewById(R.id.toolbar_profile_edit_button);
        toolBarProfileEditBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToSetupPage();
            }
        });


        /* Adding tool bar & title to main activity*/
        mToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");


        if((savedInstanceState == null) && (currentUser != null))
        {
            if(intentFrom.equals("CreatePostActivity"))
            {
                /* user redirect to feed fragment */
                bottomNavigationView.getMenu().getItem(1).setChecked(true);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, new FeedFragment());
                fragmentTransaction.commit();
            }
            else if(intentFrom.equals("ViewAnotherUserProfile"))
            {
                intentUserID = intent.getExtras().getString("intentUserID");
                currentUserID = firebaseAuth.getCurrentUser().getUid();

                /* user redirect to profile fragment */
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, new ProfileFragment());
                fragmentTransaction.commit();

                toolbarUserImage.setVisibility(View.GONE);
                toolBarProfileEditBtn.setVisibility(View.VISIBLE);

                if(!currentUserID.equals(intentUserID))
                {
                    bottomNavigationView.setVisibility(View.GONE);
                    toolbarUserImage.setVisibility(View.GONE);
                    toolBarProfileEditBtn.setVisibility(View.GONE);
                    getSupportActionBar().setTitle("Profile");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    toolbarLogo.setVisibility(View.GONE);
                    toolbarLogoText.setVisibility(View.GONE);
                }
            }
            else if(intentFrom.equals("ProfileFragment"))
            {
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, new ProfileFragment());
                fragmentTransaction.commit();

                toolbarUserImage.setVisibility(View.GONE);
                toolBarProfileEditBtn.setVisibility(View.VISIBLE);
            }
            else
            {
                /* user redirect to home fragment */
                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, new HomeFragment());
                fragmentTransaction.commit();

                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }


        if(currentUser != null)
        {
            SetToolbarUserImage();
        }


        /* Bottom navigation button click actions */
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                int id = menuItem.getItemId();

                if(id == R.id.bottom_nav_home)
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(true);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, new HomeFragment());
                    fragmentTransaction.commit();

                    toolbarUserImage.setVisibility(View.VISIBLE);
                    toolBarProfileEditBtn.setVisibility(View.GONE);
                }

                if(id == R.id.bottom_nav_feed)
                {
                    bottomNavigationView.getMenu().getItem(1).setChecked(true);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, new FeedFragment());
                    fragmentTransaction.commit();

                    toolbarUserImage.setVisibility(View.VISIBLE);
                    toolBarProfileEditBtn.setVisibility(View.GONE);
                }


                if(id == R.id.bottom_nav_services)
                {
                    bottomNavigationView.getMenu().getItem(2).setChecked(true);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, new ServicesFragment());
                    fragmentTransaction.commit();

                    toolbarUserImage.setVisibility(View.VISIBLE);
                    toolBarProfileEditBtn.setVisibility(View.GONE);
                }

                if(id == R.id.bottom_nav_profile)
                {
                    bottomNavigationView.getMenu().getItem(3).setChecked(true);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_frame, new ProfileFragment());
                    fragmentTransaction.commit();

                    toolbarUserImage.setVisibility(View.GONE);
                    toolBarProfileEditBtn.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });


        toolbarUserImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, new ProfileFragment());
                fragmentTransaction.commit();

                toolbarUserImage.setVisibility(View.GONE);
                toolBarProfileEditBtn.setVisibility(View.VISIBLE);
            }
        });

    }

    private void UserSendToSetupPage()
    {
        Intent SetupIntent = new Intent(MainActivity.this, SetupActivity.class);
        SetupIntent.putExtra("IntentFrom", "ProfileFragment");
        startActivity(SetupIntent);
    }


    /* Add validation for users */
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null)
        {
            UserSendToWelcomePage();
        }
    }


    /* retrieve user image to tool bar from firebase database */
    private void SetToolbarUserImage()
    {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("userimage"))
                    {

                        String image = dataSnapshot.child("userimage").getValue().toString();
                        if(!(image.equals("")))
                        {
                            Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.default_user_image).into(toolbarUserImage);
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



    /* Redirect user to the welcome page */
    private void UserSendToWelcomePage()
    {
        Intent welcomeIntent = new Intent(MainActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); /* Barrier for go back to main activity */
        startActivity(welcomeIntent);
        finish();
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
