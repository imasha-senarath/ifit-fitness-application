package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateServiceActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, postDatabaseReference;
    private StorageReference storageReference;

    private ImageButton firstImage, secondImage, thirdImage;
    private EditText serviceTitle, serviceSummaryDescription, serviceFullDescription, serviceSearchKeyword;
    private TextView serviceTitleError, serviceSummaryDescriptionError, serviceFullDescriptionError, serviceSearchKeywordError, serviceImageError;
    private Button submitButton;

    private Uri firstImageUri, secondImageUri, thirdImageUri;

    String currentUserID, imageType, serviceTitleValue, serviceSummaryDescriptionValue, serviceFullDescriptionValue, serviceSearchKeywordValue,
            currentTime, currentDate, serviceRandomName, firstImageDownloadURL, secondImageDownloadURL, thirdImageDownloadURL;


    private static final int Gallery_Pick = 1;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);


        /* Adding tool bar & title to add create service activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Service");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Services");


        serviceTitle = (EditText) findViewById(R.id.create_service_service_title);
        serviceSummaryDescription = (EditText) findViewById(R.id.create_service_service_summary_description);
        serviceFullDescription = (EditText) findViewById(R.id.create_service_service_full_description);
        serviceSearchKeyword = (EditText) findViewById(R.id.create_service_service_search_keyword);
        firstImage = (ImageButton) findViewById(R.id.create_service_service_first_image);
        secondImage = (ImageButton) findViewById(R.id.create_service_service_second_image);
        thirdImage = (ImageButton) findViewById(R.id.create_service_service_third_image);
        submitButton = (Button) findViewById(R.id.create_service_service_submit_button);

        serviceTitleError = (TextView) findViewById(R.id.create_service_service_title_error);
        serviceSummaryDescriptionError = (TextView) findViewById(R.id.create_service_service_summary_description_error);
        serviceFullDescriptionError = (TextView) findViewById(R.id.create_service_service_full_description_error);
        serviceSearchKeywordError = (TextView) findViewById(R.id.create_service_service_search_keyword_error);
        serviceImageError = (TextView) findViewById(R.id.create_service_service_image_error);

        loadingbar = new ProgressDialog(this);


        /* first image button click action */
        firstImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imageType = "first";
                OpenGallery();
            }
        });

        /* second image button click action */
        secondImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imageType = "second";
                OpenGallery();
            }
        });

        /* third image button click action */
        thirdImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imageType = "third";
                OpenGallery();
            }
        });

        /* submit button click action */
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateServiceInformation();
            }
        });

    }



    /* validate service information */
    private void ValidateServiceInformation()
    {
        serviceTitleValue = serviceTitle.getText().toString();
        serviceSummaryDescriptionValue = serviceSummaryDescription.getText().toString();
        serviceFullDescriptionValue = serviceFullDescription.getText().toString();
        serviceSearchKeywordValue = serviceSearchKeyword.getText().toString().toLowerCase();

        if(TextUtils.isEmpty(serviceTitleValue))
        {
            serviceTitleError.setVisibility(View.VISIBLE);
        }

        if(TextUtils.isEmpty(serviceSummaryDescriptionValue))
        {
            serviceSummaryDescriptionError.setVisibility(View.VISIBLE);
        }

        if(TextUtils.isEmpty(serviceFullDescriptionValue))
        {
            serviceFullDescriptionError.setVisibility(View.VISIBLE);
        }

        if(TextUtils.isEmpty(serviceSearchKeywordValue))
        {
            serviceSearchKeywordError.setVisibility(View.VISIBLE);
        }

        if(firstImageUri == null)
        {
            serviceImageError.setVisibility(View.VISIBLE);
        }

        if(!TextUtils.isEmpty(serviceTitleValue) && !TextUtils.isEmpty(serviceSummaryDescriptionValue) &&
                !TextUtils.isEmpty(serviceFullDescriptionValue) &&  !TextUtils.isEmpty(serviceSearchKeywordValue) &&firstImageUri != null)
        {
            /* adding Loading bar */
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Submitting...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);


            /* get current date */
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat currenDate = new SimpleDateFormat("yyyyMMdd");
            currentDate = currenDate.format(calendar1.getTime());

            /* get current time */
            Calendar calendar2 = Calendar.getInstance();
            SimpleDateFormat currenTime = new SimpleDateFormat("HHmmss");
            currentTime = currenTime.format(calendar2.getTime());

            serviceRandomName = currentDate + currentTime;

            StoreImageAndGetLink(firstImageUri, "first");

            if(secondImageUri != null)
            {
                StoreImageAndGetLink(secondImageUri, "second");
            }

            if(thirdImageUri != null)
            {
                StoreImageAndGetLink(thirdImageUri, "third");
            }
        }
    }





    /* upload images to firebase storage and get link */
    private void StoreImageAndGetLink(Uri imageUri, final String number)
    {
        final StorageReference filePath = storageReference.child("Service Images").child(imageUri.getLastPathSegment() + number + currentUserID +serviceRandomName + ".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
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
                        public void onSuccess(Uri uri)
                        {
                            if(number.equals("first"))
                            {
                                firstImageDownloadURL = uri.toString();
                            }

                            if(number.equals("second"))
                            {
                                secondImageDownloadURL = uri.toString();
                            }

                            if(number.equals("third"))
                            {
                                thirdImageDownloadURL = uri.toString();
                            }

                            SaveServiceInformationToDatabase();


                        }
                    });
                }
            }
        });
    }




    private void SaveServiceInformationToDatabase()
    {
        Calendar calendar4 = Calendar.getInstance();
        SimpleDateFormat currenDate = new SimpleDateFormat("dd MMM yyyy");
        String servicePublishDate = currenDate.format(calendar4.getTime());

        Calendar calendar3 = Calendar.getInstance();
        SimpleDateFormat currenTime = new SimpleDateFormat("hh:mm aa");
        String servicePublishTime = currenTime.format(calendar3.getTime());

        HashMap serviceMap = new HashMap();
        serviceMap.put("userid",currentUserID);
        serviceMap.put("servicedate",servicePublishDate);
        serviceMap.put("servicetime",servicePublishTime);
        serviceMap.put("servicetitle",serviceTitleValue);
        serviceMap.put("servicesummarydescription",serviceSummaryDescriptionValue);
        serviceMap.put("servicefulldescription",serviceFullDescriptionValue);
        serviceMap.put("servicesearchkeyword",serviceSearchKeywordValue);
        serviceMap.put("servicefirstimage",firstImageDownloadURL);

        if(!TextUtils.isEmpty(secondImageDownloadURL))
        {
            serviceMap.put("servicesecondimage",secondImageDownloadURL);
        }

        if(!TextUtils.isEmpty(thirdImageDownloadURL))
        {
            serviceMap.put("servicethirdimage",thirdImageDownloadURL);
        }

        postDatabaseReference.child(serviceRandomName + currentUserID).updateChildren(serviceMap).addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    loadingbar.dismiss();
                    UserSendToServicePage();
                }
            }
        });

    }

    private void UserSendToServicePage()
    {
        Intent serviceIntent = new Intent(CreateServiceActivity.this, ServiceActivity.class);
        serviceIntent.putExtra("intentFrom", "MyProfile");
        serviceIntent.putExtra("intentUserID", currentUserID);
        startActivity(serviceIntent);
        finish();
    }


    /* open the gallery and allows user to pick an image */
    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            if(imageType.equals("first"))
            {
                firstImageUri = data.getData();
                firstImage.setImageURI(firstImageUri);
            }
            else if(imageType.equals("second"))
            {
                secondImageUri = data.getData();
                secondImage.setImageURI(secondImageUri);
            }
            else if(imageType.equals("third"))
            {
                thirdImageUri = data.getData();
                thirdImage.setImageURI(thirdImageUri);
            }
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
