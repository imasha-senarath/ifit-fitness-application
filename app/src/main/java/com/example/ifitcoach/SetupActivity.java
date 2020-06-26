package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;
    private StorageReference userImgStorageReference;

    private Toolbar mToolBar;
    private CircleImageView toolbarUserImage;

    private LinearLayout usernameContainer, userBirthdayContainer, userHeightContainer, userWeightContainer;
    private RadioButton maleBtn, femaleBtn, sedentaryActiveBtn, lightlyActiveBtn, moderatelyActiveBtn, veryActiveBtn;

    private CircleImageView userImage;
    private TextView username, userPosition, userBirthday, userHeight, userWeight;

    private Button setupCompleteBtn;


    private String retrieveAccountType, retrievePosition, retrieveUserImage, retrieveUsername, retrieveBYear, retrieveBMonth, retrieveBDay, retrieveHeight,
            retrieveCurrentWeight, retrieveGender, retrieveActivityLevel;

    private String editedUsername, editedBday, editedBmonth, editedByear, editedheight, editedcurrentweight,
            editedgender, editedactivitylevel;


    private ProgressDialog loadingbar;

    String currentUserID;

    String IntentFrom;

    public int imagePick = 0;

    public Uri croppedUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Intent intent = getIntent();
        IntentFrom = intent.getExtras().getString("IntentFrom");


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userImgStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");


        /* Adding tool bar & title */
        mToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolBar);


        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        loadingbar = new ProgressDialog(this);

        usernameContainer = (LinearLayout) findViewById(R.id.setup_username_container);
        userWeightContainer = (LinearLayout) findViewById(R.id.setup_weight_container);
        userHeightContainer = (LinearLayout) findViewById(R.id.setup_height_container);
        userBirthdayContainer = (LinearLayout) findViewById(R.id.setup_birthday_container);

        userImage = (CircleImageView)findViewById(R.id.setup_user_image);
        username = (TextView)findViewById(R.id.setup_username);
        userPosition= (TextView) findViewById(R.id.setup_position);
        userWeight = (TextView)findViewById(R.id.setup_weight);
        userHeight = (TextView)findViewById(R.id.setup_height);
        userBirthday = (TextView)findViewById(R.id.setup_birthday);

        maleBtn = (RadioButton) findViewById(R.id.setup_account_male_button);
        femaleBtn = (RadioButton) findViewById(R.id.setup_account_female_button);
        sedentaryActiveBtn = (RadioButton) findViewById(R.id.setup_account_sedentary_button);
        lightlyActiveBtn = (RadioButton) findViewById(R.id.setup_account_lightly_active_button);
        moderatelyActiveBtn = (RadioButton) findViewById(R.id.setup_account_moderately_active_button);
        veryActiveBtn = (RadioButton) findViewById(R.id.setup_account_very_active_button);


        setupCompleteBtn = (Button) findViewById(R.id.setup_setup_complete_button);



        if(IntentFrom.equals("ProfileFragment"))
        {
            getSupportActionBar().setTitle("Edit Profile");
            setupCompleteBtn.setText("Update Profile");
        }
        else
        {
            getSupportActionBar().setTitle("Setup Account");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.LightTextColor)));
            mToolBar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.PrimaryTextColor), PorterDuff.Mode.SRC_ATOP);
            mToolBar.setTitleTextColor(getResources().getColor(R.color.PrimaryTextColor));
        }



        /* containers click actions */
        userImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri selectedUserImage=null;
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(SetupActivity.this);
                CropImage.activity(selectedUserImage);
            }
        });


        usernameContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenUsernameEditDialog();
            }
        });

        userBirthdayContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenBirthdayEditDialog();
            }
        });

        userHeightContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenHeightEditDialog();
            }
        });

        userWeightContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenWeightEditDialog();
            }
        });



        /* Complete button click action */
        setupCompleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SaveUserInformation();
            }
        });


        SetUserDetailsToSetupPage();
        ActivityLevelButtonActions();
        GenderButtonActions();
    }



    private void GenderButtonActions()
    {
        maleBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedgender = "Male";
                maleBtn.setChecked(true);
                femaleBtn.setChecked(false);
            }
        });


        femaleBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedgender = "Female";
                femaleBtn.setChecked(true);
                maleBtn.setChecked(false);
            }
        });
    }


    private void ActivityLevelButtonActions()
    {
        sedentaryActiveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedactivitylevel = "Sedentary";
                sedentaryActiveBtn.setChecked(true);
                lightlyActiveBtn.setChecked(false);
                moderatelyActiveBtn.setChecked(false);
                veryActiveBtn.setChecked(false);
            }
        });

        lightlyActiveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedactivitylevel = "Lightly Active";
                sedentaryActiveBtn.setChecked(false);
                lightlyActiveBtn.setChecked(true);
                moderatelyActiveBtn.setChecked(false);
                veryActiveBtn.setChecked(false);
            }
        });

        moderatelyActiveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedactivitylevel = "Moderately Active";
                sedentaryActiveBtn.setChecked(false);
                lightlyActiveBtn.setChecked(false);
                moderatelyActiveBtn.setChecked(true);
                veryActiveBtn.setChecked(false);
            }
        });

        veryActiveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editedactivitylevel = "Very Active";
                sedentaryActiveBtn.setChecked(false);
                lightlyActiveBtn.setChecked(false);
                moderatelyActiveBtn.setChecked(false);
                veryActiveBtn.setChecked(true);
            }
        });
    }


    private void OpenWeightEditDialog()
    {
        final Dialog weightdialog = new Dialog(SetupActivity.this);
        weightdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        weightdialog.setContentView(R.layout.weight_edit_layout);
        weightdialog.setTitle("weight edit window");
        weightdialog.show();
        Window weightWindow = weightdialog.getWindow();
        weightWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText editCurrentWeight = (EditText) weightdialog.findViewById(R.id.weight_edit_dialog_inputcurrentweight);
        final TextView errormsg = (TextView) weightdialog.findViewById(R.id.weight_edit_dialog_error_msg);
        errormsg.setVisibility(View.GONE);


        if(!TextUtils.isEmpty(retrieveCurrentWeight))
        {
            editCurrentWeight.setText(retrieveCurrentWeight);
        }


        if(!TextUtils.isEmpty(editedcurrentweight))
        {
            editCurrentWeight.setText(editedcurrentweight);
        }


        /* cancel button click action */
        Button cancelbtn = (Button)weightdialog.findViewById(R.id.weight_edit_dialog_cancel_button);
        cancelbtn.setEnabled(true);
        cancelbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                weightdialog.cancel();
            }
        });


        /* ok button click action */
        Button okbtn = (Button)weightdialog.findViewById(R.id.weight_edit_dialog_ok_button);
        okbtn.setEnabled(true);
        okbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.isEmpty(editCurrentWeight.getText().toString()))
                {
                    errormsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    editedcurrentweight = editCurrentWeight.getText().toString();
                    userWeight.setText((editCurrentWeight.getText().toString())+" kg");
                    weightdialog.cancel();
                }
            }
        });
    }



    private void OpenHeightEditDialog()
    {
        final Dialog heightdialog = new Dialog(SetupActivity.this);
        heightdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        heightdialog.setContentView(R.layout.height_edit_layout);
        heightdialog.setTitle("height edit window");
        heightdialog.show();
        Window heightWindow = heightdialog.getWindow();
        heightWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText editHeight = (EditText) heightdialog.findViewById(R.id.height_edit_dialog_input_height);
        final TextView errormsg = (TextView) heightdialog.findViewById(R.id.height_edit_dialog_error_msg);
        errormsg.setVisibility(View.GONE);


        if(!TextUtils.isEmpty(retrieveHeight))
        {
            editHeight.setText(retrieveHeight);
        }

        if(!TextUtils.isEmpty(editedheight))
        {
            editHeight.setText(editedheight);
        }


        /* cancel button click action */
        Button cancelbtn = (Button)heightdialog.findViewById(R.id.height_edit_dialog_cancel_button);
        cancelbtn.setEnabled(true);
        cancelbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                heightdialog.cancel();
            }
        });


        /* ok button click action */
        Button okbtn = (Button)heightdialog.findViewById(R.id.height_edit_dialog_ok_button);
        okbtn.setEnabled(true);
        okbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String tempeditedheightcm = editHeight.getText().toString();

                if(TextUtils.isEmpty(editHeight.getText().toString()))
                {
                    errormsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    editedheight = editHeight.getText().toString();
                    userHeight.setText(editHeight.getText().toString()+" cm");
                    heightdialog.cancel();
                }
            }
        });
    }



    private void OpenBirthdayEditDialog()
    {
        final Dialog birthdaydialog = new Dialog(SetupActivity.this);
        birthdaydialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        birthdaydialog.setContentView(R.layout.birthday_edit_layout);
        birthdaydialog.setTitle("birthday edit window");
        birthdaydialog.show();
        Window birthdayWindow = birthdaydialog.getWindow();
        birthdayWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final TextView errormsg = (TextView)birthdaydialog.findViewById(R.id.birthday_edit_dialog_error_msg);
        errormsg.setVisibility(View.GONE);

        final EditText editBday = (EditText)birthdaydialog.findViewById(R.id.birthday_edit_dialog_inputbday);
        final EditText editBmonth = (EditText)birthdaydialog.findViewById(R.id.birthday_edit_dialog_inputbmonth);
        final EditText editByear = (EditText)birthdaydialog.findViewById(R.id.birthday_edit_dialog_inputbyear);

        Button cancelbtn = (Button)birthdaydialog.findViewById(R.id.birthday_edit_dialog_cancel_button);
        Button okbtn = (Button)birthdaydialog.findViewById(R.id.birthday_edit_dialog_ok_button);


        /*hide edit text & set text to edit text - check database value */
        if( (!TextUtils.isEmpty(retrieveBDay) && !TextUtils.isEmpty(retrieveBMonth)) && !TextUtils.isEmpty(retrieveBYear))
        {
            editBday.setText(retrieveBDay);
            editBmonth.setText(retrieveBMonth);
            editByear.setText(retrieveBYear);
        }


        if((!TextUtils.isEmpty(editedBday) && !TextUtils.isEmpty(editedBmonth)) && !TextUtils.isEmpty(editedByear))
        {
            editBday.setText(editedBday);
            editBmonth.setText(editedBmonth);
            editByear.setText(editedByear);
        }


        /* cancel button click action */
        cancelbtn.setEnabled(true);
        cancelbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                birthdaydialog.cancel();
            }
        });


        /* save button click action */
        okbtn.setEnabled(true);
        okbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);

                String tempeditedBday = editBday.getText().toString();
                String tempeditedBmonth = editBmonth.getText().toString();
                String tempeditedByear = editByear.getText().toString();

                if((tempeditedBday.equals("")) || (tempeditedBday.length() > 2) || ((Integer.parseInt(tempeditedBday)) > 31)  || ((Integer.parseInt(tempeditedBday)) < 1)
                        || (tempeditedBmonth.equals("")) || (tempeditedBmonth.length() > 2) || ((Integer.parseInt(tempeditedBmonth)) > 12) || ((Integer.parseInt(tempeditedBmonth)) < 1)
                        || (tempeditedByear.equals("")) || (tempeditedByear.length() > 4) || ((Integer.parseInt(tempeditedByear)) > currentYear) || ((Integer.parseInt(tempeditedByear)) < 1900))
                {
                    errormsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    editedBday = tempeditedBday;
                    editedBmonth = tempeditedBmonth;
                    editedByear = tempeditedByear;

                    userBirthday.setText(editedBday+" / "+editedBmonth+" / "+editedByear);
                    birthdaydialog.cancel();
                }
            }
        });
    }



    private void OpenUsernameEditDialog()
    {
        final Dialog usernamedialog = new Dialog(SetupActivity.this);
        usernamedialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        usernamedialog.setContentView(R.layout.username_edit_layout);
        usernamedialog.setTitle("username edit window");
        usernamedialog.show();
        Window usernameWindow =  usernamedialog.getWindow();
        usernameWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final TextView errormsg = (TextView) usernamedialog.findViewById(R.id.username_edit_dialog_error_msg);
        errormsg.setVisibility(View.GONE);

        final EditText editUsername = (EditText)usernamedialog.findViewById(R.id.username_edit_dialog_input);

        if(!(TextUtils.isEmpty(retrieveUsername)))
        {
            editUsername.setText(retrieveUsername);
        }


        if(!(TextUtils.isEmpty(editedUsername)))
        {
            editUsername.setText(editedUsername);
        }


        /* cancel button click action */
        Button cancelbtn = (Button)usernamedialog.findViewById(R.id.username_edit_dialog_cancel_button);
        cancelbtn.setEnabled(true);
        cancelbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                usernamedialog.cancel();
            }
        });

        /* save button click action */
        Button okbtn = (Button)usernamedialog.findViewById(R.id.username_edit_dialog_ok_button);
        okbtn.setEnabled(true);
        okbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String tempeditedUsername = editUsername.getText().toString();
                if((tempeditedUsername.equals("")) || (tempeditedUsername.length() < 4))
                {
                    errormsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    editedUsername = tempeditedUsername;
                    username.setText(editedUsername);
                    usernamedialog.cancel();
                }
            }
        });
    }



    /* getting user details from database & set to setup page */
    private void SetUserDetailsToSetupPage()
    {
        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username"))
                    {
                        retrieveUsername = dataSnapshot.child("username").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveUsername))
                        {
                            username.setText(retrieveUsername);
                        }
                    }

                   if((dataSnapshot.hasChild("userbyear")) && (dataSnapshot.hasChild("userbday")) && (dataSnapshot.hasChild("userbmonth")))
                    {
                        retrieveBYear = dataSnapshot.child("userbyear").getValue().toString();
                        retrieveBDay = dataSnapshot.child("userbday").getValue().toString();
                        retrieveBMonth = dataSnapshot.child("userbmonth").getValue().toString();
                        if((!TextUtils.isEmpty(retrieveBYear)) && (!TextUtils.isEmpty(retrieveBDay)) && (!TextUtils.isEmpty(retrieveBMonth)))
                        {
                            userBirthday.setText(retrieveBDay+" / "+retrieveBMonth+" / "+retrieveBYear);
                        }
                    }

                    if(dataSnapshot.hasChild("userimage"))
                    {
                        retrieveUserImage = dataSnapshot.child("userimage").getValue().toString();
                        if(!(TextUtils.isEmpty(retrieveUserImage)))
                        {
                            Picasso.with(SetupActivity.this).load(retrieveUserImage).placeholder(R.drawable.default_user_image).into(userImage);
                        }
                    }

                   if(dataSnapshot.hasChild("userheight"))
                    {
                        retrieveHeight = dataSnapshot.child("userheight").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveHeight))
                        {
                            userHeight.setText(retrieveHeight+" cm");
                        }
                    }


                   if(dataSnapshot.hasChild("usercurrentweight"))
                   {
                       retrieveCurrentWeight = dataSnapshot.child("usercurrentweight").getValue().toString();
                       if(!TextUtils.isEmpty(retrieveCurrentWeight))
                       {
                           userWeight.setText(retrieveCurrentWeight+" kg");
                       }
                   }


                    if(dataSnapshot.hasChild("usergender"))
                    {
                        retrieveGender = dataSnapshot.child("usergender").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveGender))
                        {
                            if(retrieveGender.equals("Male"))
                            {
                                maleBtn.setChecked(true);
                            }

                            if(retrieveGender.equals("Female"))
                            {
                                femaleBtn.setChecked(true);
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("useractivitylevel"))
                    {
                        retrieveActivityLevel = dataSnapshot.child("useractivitylevel").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveActivityLevel))
                        {
                            if(retrieveActivityLevel.equals("Sedentary"))
                            {
                                sedentaryActiveBtn.setChecked(true);
                            }
                            if(retrieveActivityLevel.equals("Lightly Active"))
                            {
                                lightlyActiveBtn.setChecked(true);
                            }
                            if(retrieveActivityLevel.equals("Moderately Active"))
                            {
                                moderatelyActiveBtn.setChecked(true);
                            }
                            if(retrieveActivityLevel.equals("Very Active"))
                            {
                                veryActiveBtn.setChecked(true);
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("useraccounttype"))
                    {
                        retrieveAccountType = dataSnapshot.child("useraccounttype").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveAccountType))
                        {
                            if(retrieveAccountType.equals("BUY A SERVICE"))
                            {
                                userPosition.setVisibility(View.GONE);
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("userposition"))
                    {
                        retrievePosition = dataSnapshot.child("userposition").getValue().toString();
                        if(!TextUtils.isEmpty(retrievePosition))
                        {
                            userPosition.setText(retrievePosition);
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





    /* user image crop activities  */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode == RESULT_OK)
                {
                    croppedUserImage = result.getUri();
                    Picasso.with(SetupActivity.this).load(croppedUserImage.toString()).placeholder(R.drawable.default_user_image).into(userImage);
                    imagePick= 1;
                }
            }
    }



    /* send user details to database */
    private void SaveUserInformation()
    {
        /* adding Loading bar */
        if(IntentFrom.equals("ProfileFragment"))
        {
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Updating...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);
        }
        else
        {
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Completing...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);
        }


        HashMap usermap = new HashMap();

        /* username */
        if(!TextUtils.isEmpty(editedUsername))
        {
            usermap.put("usersearchkeyword", editedUsername.toLowerCase());
            usermap.put("username", editedUsername);
        }

        /* userweight */
        if(!TextUtils.isEmpty(editedcurrentweight))
        {
            usermap.put("usercurrentweight", editedcurrentweight);
        }

        /* userbirthday */
        if(!TextUtils.isEmpty(editedBday) && !TextUtils.isEmpty(editedBmonth) && !TextUtils.isEmpty(editedByear))
        {
            usermap.put("userbday", editedBday);
            usermap.put("userbmonth", editedBmonth);
            usermap.put("userbyear", editedByear);
        }

        /* userheight */
        if(!TextUtils.isEmpty(editedheight))
        {
            usermap.put("userheight", editedheight);
        }

        /* usergender */
        if(!TextUtils.isEmpty(editedgender))
        {
            usermap.put("usergender", editedgender);
        }

        /* useractivitylevel */
        if(!TextUtils.isEmpty(editedactivitylevel))
        {
            usermap.put("useractivitylevel", editedactivitylevel);
        }

        userDatabaseReference.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener()
        {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        if(imagePick == 1)
                        {
                            SaveUserImage();
                        }
                        else
                        {
                            if(IntentFrom.equals("ProfileFragment"))
                            {
                                SendUserToMainPage();
                                Toast.makeText(SetupActivity.this, "Your Profile Update Successfully", Toast.LENGTH_SHORT).show();
                            }
                            if(IntentFrom.equals("CreateAccountActivity"))
                            {
                                SendUserToMainPage();
                                Toast.makeText(SetupActivity.this, "Your Account Setup Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else
                    {
                        loadingbar.dismiss();
                        String msg = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error : " + msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }



    private void SaveUserImage()
    {
        final StorageReference filePath = userImgStorageReference.child(currentUserID + ".jpg");

        /* save image to firebase storage */
        filePath.putFile(croppedUserImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    /* get image link from firebase storage */
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri aboutmeUserImageCropResultUri)
                        {
                            final String userImageDownloadUrl = aboutmeUserImageCropResultUri.toString();

                            /* save image link to firebase database */
                            HashMap usermap = new HashMap();
                            usermap.put("userimage", userImageDownloadUrl);
                            userDatabaseReference.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener()
                            {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingbar.dismiss();


                                        if(IntentFrom.equals("ProfileFragment"))
                                        {
                                            SendUserToMainPage();
                                            Toast.makeText(SetupActivity.this, "Your Profile Update Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        if(IntentFrom.equals("CreateAccountActivity"))
                                        {
                                            SendUserToMainPage();
                                            Toast.makeText(SetupActivity.this, "Your Account Setup Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        loadingbar.dismiss();
                                        String msg = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
                else
                {
                    String msg2 = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error : "+msg2, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    /* redirect to main page */
    private void SendUserToMainPage()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("intentFrom", IntentFrom);
        startActivity(mainIntent);
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
