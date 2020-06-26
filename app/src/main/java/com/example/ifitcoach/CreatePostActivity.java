package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class CreatePostActivity extends AppCompatActivity
{

    private Toolbar mainToolBar;
    private CircleImageView  toolbarUserImage;

    private ProgressDialog loadingbar;

    private CircleImageView userImage;
    private TextView userName;

    private ImageView firstImage, secondImage, thirdImage;
    private EditText postDescription;
    private FloatingActionButton addImageButton, postCompleteButton;

    private static final int Gallery_Pick = 1;

    private Uri firstImageUri, secondImageUri, thirdImageUri;

    private String retrieveUserName, retrieveUserImage, imageType, description="", saveCurrentDate, saveCurrentTime, postRandomName,
            firstImageDownloadUrl, secondImageDownloadUrl, thirdImageDownloadUrl;

    private String editPostDescription, editPostFirstImage, editPostSecondImage, editPostThirdImage;

    private StorageReference storageReference;
    private DatabaseReference userDatabaseReference, postDatabaseReference;

    private FirebaseAuth firebaseAuth;

    String currentUserID;

    String intentFrom, intentPostID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        /* Adding tool bar & title to Create post activity*/
        mainToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);



        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("FeedFragmentToEditPost") || intentFrom.equals("ProfileActivityToEditPost"))
        {
            intentPostID = intent.getExtras().getString("intentPostID");
        }


        userImage = (CircleImageView) findViewById(R.id.createpost_profile_image);
        userName = (TextView) findViewById(R.id.createpost_username);

        postDescription = (EditText) findViewById(R.id.createpost_description_text);
        addImageButton = (FloatingActionButton) findViewById(R.id.createpost_add_button);
        postCompleteButton = (FloatingActionButton) findViewById(R.id.createpost_post_complete_button);
        firstImage = (ImageView) findViewById(R.id.createpost_first_image);
        secondImage = (ImageView) findViewById(R.id.createpost_second_image);
        thirdImage = (ImageView) findViewById(R.id.createpost_third_image);
        loadingbar = new ProgressDialog(this);


        /* if intent to edit post, */
        if(intentFrom.equals("FeedFragmentToEditPost") || intentFrom.equals("ProfileActivityToEditPost"))
        {
            getSupportActionBar().setTitle("Edit Post");
            addImageButton.setVisibility(View.GONE);

            if(!TextUtils.isEmpty(intentPostID))
            {
                LoadPostDetailsToEditPage();
            }

        }

        if(intentFrom.equals("ProfileActivityToCreatePost"))
        {
            postDescription.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(postDescription, InputMethodManager.SHOW_IMPLICIT);
        }


        SetUserDetails();


        /* post button click action */
        postCompleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(intentFrom.equals("FeedFragmentToEditPost") || intentFrom.equals("ProfileActivityToEditPost"))
                {
                    ValidateAndSaveEditedPost();
                }
                else
                {
                    ValidatePostInformation();
                }

            }
        });


        addImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(firstImageUri == null)
                {
                    imageType = "first";
                    OpenGallery();
                }

                else if(secondImageUri == null)
                {
                    imageType = "second";
                    OpenGallery();
                }
                else if(thirdImageUri == null)
                {
                    imageType = "third";
                    OpenGallery();
                }
                else
                {
                    Toast.makeText(CreatePostActivity.this, "You can only add a maximum of 3 photos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void SetUserDetails()
    {
        userDatabaseReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("userimage"))
                    {
                        retrieveUserImage = dataSnapshot.child("userimage").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveUserImage))
                        {
                            Picasso.with(CreatePostActivity.this).load(retrieveUserImage).placeholder(R.drawable.default_user_image).into(userImage);
                        }
                    }
                    if(dataSnapshot.hasChild("username"))
                    {
                        retrieveUserName = dataSnapshot.child("username").getValue().toString();
                        if(!TextUtils.isEmpty(retrieveUserName))
                        {
                            userName.setText(retrieveUserName);
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



    private void ValidateAndSaveEditedPost()
    {
        editPostDescription = postDescription.getText().toString();

        if(TextUtils.isEmpty(editPostFirstImage) && TextUtils.isEmpty(editPostSecondImage) && TextUtils.isEmpty(editPostThirdImage)
                && TextUtils.isEmpty(postDescription.getText().toString().trim()))
        {
            Toast.makeText(this, "Post is empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            /* adding Loading bar */
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Saving...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);

            HashMap postMap = new HashMap();
            postMap.put("postdescription",editPostDescription);


            postDatabaseReference.child(intentPostID).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        if(intentFrom.equals("FeedFragmentToEditPost"))
                        {
                            SendUserToMainPage();
                        }
                        else
                        {
                            SendUserToProfilePage();
                        }

                        Toast.makeText(CreatePostActivity.this, "Post is updated succesfully.", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else
                    {
                        String msg = task.getException().getMessage();
                        Toast.makeText(CreatePostActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }



    private void LoadPostDetailsToEditPage()
    {
        postDatabaseReference.child(intentPostID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("postdescription"))
                    {
                        editPostDescription = dataSnapshot.child("postdescription").getValue().toString();
                        if(!TextUtils.isEmpty(editPostDescription))
                        {
                            postDescription.setText(editPostDescription);
                        }
                    }

                    if(dataSnapshot.hasChild("postfirstimage"))
                    {
                        editPostFirstImage = dataSnapshot.child("postfirstimage").getValue().toString();
                        if(!TextUtils.isEmpty(editPostFirstImage))
                        {
                            Picasso.with(CreatePostActivity.this).load(editPostFirstImage).into(firstImage);
                            firstImage.setVisibility(View.VISIBLE);
                        }
                    }

                    if(dataSnapshot.hasChild("postsecondimage"))
                    {
                        editPostSecondImage = dataSnapshot.child("postsecondimage").getValue().toString();
                        if(!TextUtils.isEmpty(editPostFirstImage))
                        {
                            Picasso.with(CreatePostActivity.this).load(editPostSecondImage).into(secondImage);
                            secondImage.setVisibility(View.VISIBLE);
                        }
                    }

                    if(dataSnapshot.hasChild("postthirdimage"))
                    {
                        editPostThirdImage = dataSnapshot.child("postthirdimage").getValue().toString();
                        if(!TextUtils.isEmpty(editPostThirdImage))
                        {
                            Picasso.with(CreatePostActivity.this).load(editPostThirdImage).into(thirdImage);
                            thirdImage.setVisibility(View.VISIBLE);
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


    private void ValidatePostInformation()
    {
        description = postDescription.getText().toString();

        if(firstImageUri == null && secondImageUri == null && thirdImageUri == null && (TextUtils.isEmpty(postDescription.getText().toString().trim())))
        {
            Toast.makeText(this, "Post is empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            /* adding Loading bar */
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Uploading...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);


            /* get current date */
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat currenDate = new SimpleDateFormat("yyyyMMdd");
            saveCurrentDate = currenDate.format(calendar1.getTime());

            /* get current time */
            Calendar calendar2 = Calendar.getInstance();
            SimpleDateFormat currenTime = new SimpleDateFormat("HHmmss");
            saveCurrentTime = currenTime.format(calendar2.getTime());


            postRandomName = saveCurrentDate + saveCurrentTime;

            if(firstImageUri != null || secondImageUri != null || thirdImageUri != null)
            {
                if(firstImageUri != null)
                {
                    StoreImageToFirebaseStorage(firstImageUri, "first");
                }

                if(secondImageUri != null)
                {
                    StoreImageToFirebaseStorage(secondImageUri, "second");
                }

                if(thirdImageUri != null)
                {
                    StoreImageToFirebaseStorage(thirdImageUri, "third");
                }
            }
            else
            {
                SavePostInformationToDatabase();
            }
        }

    }


    /* save image to storage & get link of image */
    private void StoreImageToFirebaseStorage(Uri imageUri, final String number)
    {
        final StorageReference filePath = storageReference.child("Post Images").child(imageUri.getLastPathSegment() + number + currentUserID + postRandomName + ".jpg");

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
                        public void onSuccess(Uri aboutmeUserImageCropResultUri)
                        {
                            if(number.equals("first"))
                            {
                                firstImageDownloadUrl = aboutmeUserImageCropResultUri.toString();
                            }

                            if(number.equals("second"))
                            {
                                secondImageDownloadUrl = aboutmeUserImageCropResultUri.toString();
                            }

                            if(number.equals("third"))
                            {
                                thirdImageDownloadUrl = aboutmeUserImageCropResultUri.toString();
                            }

                            SavePostInformationToDatabase();
                        }
                    });
                }
                else
                {
                    String msg = task.getException().getMessage();
                    Toast.makeText(CreatePostActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    /* save post information to firebase database */
    private void SavePostInformationToDatabase()
    {
        Calendar calendar4 = Calendar.getInstance();
        SimpleDateFormat currenDate = new SimpleDateFormat("dd MMM yyyy");
        String currentDate = currenDate.format(calendar4.getTime());

        Calendar calendar3 = Calendar.getInstance();
        SimpleDateFormat currenTime = new SimpleDateFormat("hh:mm aa");
        String currentTime = currenTime.format(calendar3.getTime());


        HashMap postMap = new HashMap();
        postMap.put("userid",currentUserID);
        postMap.put("postdate",currentDate);
        postMap.put("posttime",currentTime);

        if(!TextUtils.isEmpty(description))
        {
            postMap.put("postdescription",description);
        }
        if(firstImageUri != null)
        {
            postMap.put("postfirstimage",firstImageDownloadUrl);
        }
        if(secondImageUri != null)
        {
            postMap.put("postsecondimage",secondImageDownloadUrl);
        }
        if(thirdImageUri != null)
        {
            postMap.put("postthirdimage",thirdImageDownloadUrl);
        }

        postDatabaseReference.child(postRandomName + currentUserID).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    if(intentFrom.equals("ProfileActivityToCreatePost"))
                    {
                        SendUserToProfilePage();
                    }
                    else
                    {
                        SendUserToMainPage();
                    }

                    Toast.makeText(CreatePostActivity.this, "Post is uploaded succesfully", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
                else
                {
                    String msg = task.getException().getMessage();
                    Toast.makeText(CreatePostActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });

    }




    /* open gallery & pick image */
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
                /* display preview of image */
                firstImageUri = data.getData();
                firstImage.setImageURI(firstImageUri);
                firstImage.setVisibility(View.VISIBLE);
            }

            if(imageType.equals("second"))
            {
                /* display preview of image */
                secondImageUri = data.getData();
                secondImage.setImageURI(secondImageUri);
                secondImage.setVisibility(View.VISIBLE);
            }

            if(imageType.equals("third"))
            {
                /* display preview of image */
                thirdImageUri = data.getData();
                thirdImage.setImageURI(thirdImageUri);
                thirdImage.setVisibility(View.VISIBLE);
            }
        }
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    /* redirect to main page */
    private void SendUserToMainPage()
    {
        Intent mainIntent = new Intent (CreatePostActivity.this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "CreatePostActivity");
        startActivity(mainIntent);
        finish();
    }

    /* redirect to profile page */
    private void SendUserToProfilePage()
    {
        Intent profileIntent = new Intent (CreatePostActivity.this, PostActivity.class);
        profileIntent.putExtra("intentFrom", "CreatePostActivity");
        startActivity(profileIntent);
        finish();
    }

}
