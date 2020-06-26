package com.example.ifitcoach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity
{

    private Button signUpBtn, loginBtn;

    private TextView text1, text2;

    private ImageView backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);

        backgroundImage = (ImageView) findViewById(R.id.welcome_background_image);


        /* add animation to image */
        Animation imageAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.welcome_image_animation);
        backgroundImage.startAnimation(imageAnimation);

        /* add animation to text */
        Animation text1Animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.welcome_text1_animation);
        text1.startAnimation(text1Animation);
        Animation text2Animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.welcome_text2_animation);
        text2.startAnimation(text2Animation);


        signUpBtn = (Button)findViewById(R.id.welcome_signup_button);
        loginBtn = (Button)findViewById(R.id.register_facebook_button);




        /* Signup button click action */
        signUpBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                SendUserToAccountTypePage();
            }
        });


        /* login button click action */
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginPage();
            }
        });
    }



    /* Redirect to login page */
    private void SendUserToLoginPage()
    {
        Intent loginIntent = new Intent(WelcomeActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }


    /* Redirect to acount type page */
    private void SendUserToAccountTypePage()
    {
        Intent accountTypeIntent = new Intent(WelcomeActivity.this,AccountTypeActivity.class);
        startActivity(accountTypeIntent);
    }
}
