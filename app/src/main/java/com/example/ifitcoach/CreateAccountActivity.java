package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAccountActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;

    private ProgressDialog loadingBar;

    private TextInputLayout createAccountAccountType, createAccountPosition, createAccountUsername, createAccountEmail, createAccountPassword, createAccountConfirmPassword;

    private Button createAccountBtn;

    private String userAccountType, userPosition, username, userEmail, userPassword, userConfirmPassword;

    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);


        /* Adding tool bar with title and hiding user image */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.LightTextColor)));
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.PrimaryTextColor), PorterDuff.Mode.SRC_ATOP);
        toolbar.setTitleTextColor(getResources().getColor(R.color.PrimaryTextColor));

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        createAccountAccountType = (TextInputLayout) findViewById(R.id.create_account_account_type);
        createAccountPosition = (TextInputLayout) findViewById(R.id.create_account_position);
        createAccountEmail = (TextInputLayout) findViewById(R.id.create_account_email);
        createAccountUsername = (TextInputLayout) findViewById(R.id.create_account_username);
        createAccountPassword = (TextInputLayout) findViewById(R.id.create_account_password);
        createAccountConfirmPassword = (TextInputLayout) findViewById(R.id.create_account_confirm_password);
        createAccountBtn = (Button) findViewById(R.id.register_create_account_button);


        Intent intent = getIntent();
        userAccountType = intent.getExtras().getString("AccountType");
        userPosition = intent.getExtras().getString("Position");
        if(userAccountType.equals("BUY A SERVICE"))
        {
            createAccountPosition.setVisibility(View.GONE);
            createAccountAccountType.getEditText().setText("FIND A SERVICE");
        }

        if(userAccountType.equals("SELL A SERVICE"))
        {
            createAccountPosition.getEditText().setText(userPosition);
            createAccountAccountType.getEditText().setText("BECOME A SELLER");
        }


        /* Create account button action */
        createAccountBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateNewAccount();
            }
        });
    }




    /* get user email, password & authenticate user to login */
    private void CreateNewAccount()
    {
        username = createAccountUsername.getEditText().getText().toString();
        userEmail = createAccountEmail.getEditText().getText().toString();
        userPassword = createAccountPassword.getEditText().getText().toString();
        userConfirmPassword = createAccountConfirmPassword.getEditText().getText().toString();


        if(TextUtils.isEmpty(username))
        {
            createAccountUsername.setError("Please enter username.");
        }
        else
        {
            createAccountUsername.setError(null);
        }


        if(TextUtils.isEmpty(userEmail))
        {
            createAccountEmail.setError("Please enter email.");
        }
        else
        {
            createAccountEmail.setError(null);
        }


        if(TextUtils.isEmpty(userPassword))
        {
            createAccountPassword.setError("Please enter password.");
        }
        else
        {
            createAccountPassword.setError(null);
        }


        if(TextUtils.isEmpty(userConfirmPassword))
        {
            createAccountConfirmPassword.setError("Please confirm the password.");
        }
        else
        {
            createAccountConfirmPassword.setError(null);
        }


        if(!TextUtils.isEmpty(userPassword) && !TextUtils.isEmpty(userConfirmPassword))
        {
            if (!userPassword.equals(userConfirmPassword))
            {
                createAccountConfirmPassword.setError("Please doesn't match.");
            }
            else
            {
                createAccountConfirmPassword.setError(null);
            }
        }


        if( !TextUtils.isEmpty(username) && !TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword)
                && !TextUtils.isEmpty(userConfirmPassword) && userPassword.equals(userConfirmPassword))
        {

            /* adding Loading bar */
            loadingBar = new ProgressDialog(this);
            String ProgressDialogMessage="Creating Account...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingBar.setMessage(spannableMessage);
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.setCancelable(false);


            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        AddOtherDetailsToDatabase();
                    }
                    else
                    {
                        createAccountPassword.getEditText().setText("");
                        createAccountConfirmPassword.getEditText().setText("");

                        loadingBar.dismiss();

                        String msg = task.getException().getMessage();

                        /* error dialog box */
                        final Dialog errorDialog = new Dialog(CreateAccountActivity.this);
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


    /* add name & other details to firebase database */
    private void AddOtherDetailsToDatabase()
    {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        String joinedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        HashMap usermap = new HashMap();
        usermap.put("useraccounttype", userAccountType);
        if(userAccountType.equals("SELL A SERVICE"))
        {
            usermap.put("userposition", userPosition);
        }
        usermap.put("usersearchkeyword", username.toLowerCase());
        usermap.put("username", username);
        usermap.put("useremail", userEmail);
        usermap.put("userjoineddate", joinedDate);

        userDatabaseReference.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    loadingBar.dismiss();
                    SendUserToSetupPage();
                }
                else
                {
                    createAccountPassword.getEditText().setText("");
                    createAccountConfirmPassword.getEditText().setText("");

                    loadingBar.dismiss();
                    String msg2 = task.getException().getMessage();

                    /* error dialog box */
                    final Dialog errordialog = new Dialog(CreateAccountActivity.this);
                    errordialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    errordialog.setContentView(R.layout.error_layout);
                    errordialog.setTitle("Error Window");
                    errordialog.show();
                    errordialog.setCanceledOnTouchOutside(false);

                    TextView error = (TextView)errordialog.findViewById(R.id.error_dialog_error_message);
                    error.setText(msg2);

                    Button cancelBtn = (Button)errordialog.findViewById(R.id.error_dialog_cancel_button);
                    cancelBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            errordialog.cancel();
                        }
                    });

                }
            }
        });
    }

    /* Redirect to setup page */
    private void SendUserToSetupPage()
    {
        Intent setupIntent = new Intent(CreateAccountActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setupIntent.putExtra("IntentFrom", "CreateAccountActivity");
        startActivity(setupIntent);
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
