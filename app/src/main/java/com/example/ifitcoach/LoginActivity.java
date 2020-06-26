package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.AccountType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth mAuth;

    private TextInputLayout loginInputEmail, loginInputPassword;

    private Button loginBtn;
    private TextView createAccountBtn;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Adding tool bar with title and hiding user image */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Welcome Back");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.LightTextColor)));
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.PrimaryTextColor), PorterDuff.Mode.SRC_ATOP);
        toolbar.setTitleTextColor(getResources().getColor(R.color.PrimaryTextColor));

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        mAuth = FirebaseAuth.getInstance();

        loginInputEmail = (TextInputLayout) findViewById(R.id.login_email);
        loginInputPassword = (TextInputLayout) findViewById(R.id.login_password);

        createAccountBtn = (TextView) findViewById(R.id.login_create_new_account_button);

        loginBtn = (Button) findViewById(R.id.login_login_button);
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AllowingUserToLog();
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAccountTypePage();
            }
        });
    }



    /* check whether user already authenticate or not */
    private void AllowingUserToLog()
    {
        String email = loginInputEmail.getEditText().getText().toString().trim();
        String password = loginInputPassword.getEditText().getText().toString().trim();

        if(email.isEmpty())
        {
            loginInputEmail.setError("Please enter email address!");
        }
        else
        {
            loginInputEmail.setError(null);
        }

        if(password.isEmpty())
        {
            loginInputPassword.setError("Please enter password!");
        }
        else
        {
            loginInputPassword.setError(null);
        }

        if(!email.isEmpty() && !password.isEmpty())
        {
            /* adding Loading bar */
            loadingBar = new ProgressDialog(this);
            String ProgressDialogMessage="Logging...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingBar.setMessage(spannableMessage);
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.setCancelable(false);


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainPage();
                    }
                    else
                    {
                        loadingBar.cancel();
                        loginInputPassword.getEditText().setText("");

                        /* display error to user */
                        String msg = task.getException().getMessage();

                        final Dialog errorDialog = new Dialog(LoginActivity.this);
                        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        errorDialog.setContentView(R.layout.error_layout);
                        errorDialog.setTitle("Error Window");
                        errorDialog.show();
                        errorDialog.setCanceledOnTouchOutside(false);

                        TextView error = (TextView)errorDialog.findViewById(R.id.error_dialog_error_message);
                        error.setText(msg);

                        Button cancelBtn = (Button)errorDialog.findViewById(R.id.error_dialog_cancel_button);
                        cancelBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                errorDialog.cancel();
                            }
                        });
                    }
                }
            });
        }
    }




    /* User redirect to main page*/
    private void SendUserToMainPage()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("intentFrom", "LoginActivity");
        startActivity(mainIntent);
        finish();
    }

    private void UserSendToAccountTypePage()
    {
        Intent accountTypeIntent = new Intent(LoginActivity.this, AccountTypeActivity.class);
        startActivity(accountTypeIntent);
        finish();
    }

    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
