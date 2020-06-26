package com.example.ifitcoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountTypeActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private RadioButton buyAServiceBtn, sellAServiceBtn;
    private Spinner position;
    private TextView errorText, loginBtn;
    private Button nextBtn;

    private String strPosition, strAccountType="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        /* Adding tool bar with title and hiding user image */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Type");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.LightTextColor)));
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.PrimaryTextColor), PorterDuff.Mode.SRC_ATOP);
        toolbar.setTitleTextColor(getResources().getColor(R.color.PrimaryTextColor));

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);

        buyAServiceBtn = (RadioButton) findViewById(R.id.account_type_buy_a_service_button);
        sellAServiceBtn = (RadioButton) findViewById(R.id.account_type_sell_a_service_button);

        errorText = (TextView) findViewById(R.id.account_type_error_text);
        errorText.setVisibility(View.INVISIBLE);
        nextBtn = (Button) findViewById(R.id.account_type_next_button);
        loginBtn = (TextView) findViewById(R.id.account_type_login_button);

        position = findViewById(R.id.account_type_position);
        position.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.position, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        position.setAdapter(adapter);
        position.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                strPosition = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });



        /* button one click action */
        buyAServiceBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                strAccountType = "BUY A SERVICE";

                buyAServiceBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                sellAServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                sellAServiceBtn.setChecked(false);
                position.setVisibility(View.GONE);
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        /* button two click action */
        sellAServiceBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                strAccountType = "SELL A SERVICE";

                sellAServiceBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                buyAServiceBtn.setTextColor(getResources().getColor(R.color.PrimaryTextColor));
                buyAServiceBtn.setChecked(false);
                position.setVisibility(View.VISIBLE);
                errorText.setVisibility(View.INVISIBLE);
            }
        });


        nextBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                NextButtonActions();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToLoginPage();
            }
        });

    }



    private void NextButtonActions()
    {
        if(strAccountType.isEmpty())
        {
            errorText.setText("Please select the account type.");
            errorText.setVisibility(View.VISIBLE);
        }

        if(strAccountType.equals("BUY A SERVICE"))
        {
            UserSendToRegistrationPage("BUY A SERVICE","null");
        }

        if(strAccountType.equals("SELL A SERVICE"))
        {
            if(strPosition.isEmpty() || strPosition.equals("Select Your Position"))
            {
                errorText.setText("Please select your position.");
                errorText.setVisibility(View.VISIBLE);
            }
            else
            {
                UserSendToRegistrationPage("SELL A SERVICE",strPosition);
            }
        }
    }



    private void UserSendToLoginPage()
    {
        Intent loginIntent = new Intent(AccountTypeActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    /* redirect to registration page as a buyer */
    private void UserSendToRegistrationPage(String accountType, String position)
    {
        Intent createAccountIntent = new Intent(AccountTypeActivity.this, CreateAccountActivity.class);
        createAccountIntent.putExtra("AccountType", accountType);
        createAccountIntent.putExtra("Position", position);
        startActivity(createAccountIntent);
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

}
