package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, postDatabaseReference, postDeleteDatabaseReference, commentDatabaseReference
            ,voteDatabaseReference, voteDeleteDatabaseReference, postCommentDeleteDatabaseReference, postVoteDeleteDatabaseReference;

    String currentUserID;

    String intentFrom, intentUserID;

    String retrieveUserName, retrieveUserPosition, retrieveUserImage="";

    private FloatingActionButton createPostsBtn;

    private RecyclerView postList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);



        /* Adding tool bar & title to profile activity*/
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("ViewAnotherUserProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        commentDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Comments");
        voteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Votes");


        createPostsBtn = (FloatingActionButton) findViewById(R.id.post_create_post_button);
        createPostsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToCreatePostPage();
            }
        });



        postList = (RecyclerView)findViewById(R.id.profile_post_list);
        postList.setNestedScrollingEnabled(false);
        //postList.setHasFixedSize(true); //if add this line, sometimes disable scroll.


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define post order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            SetUserDetailsToPostPage(intentUserID);
            DisplayUserAllPosts(intentUserID);
            if(!intentUserID.equals(currentUserID))
            {
                createPostsBtn.setVisibility(View.GONE);
            }
        }
        else
        {
            SetUserDetailsToPostPage(currentUserID);
            DisplayUserAllPosts(currentUserID);
        }

    }



    /* set user details to profile page top container */
    private void SetUserDetailsToPostPage(String userID)
    {
        userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    /* check whether userimage exist or not firebase database */
                    if(dataSnapshot.hasChild("userimage"))
                    {
                        retrieveUserImage = dataSnapshot.child("userimage").getValue().toString();

                    }
                    if(dataSnapshot.hasChild("username"))
                    {
                        retrieveUserName = dataSnapshot.child("username").getValue().toString();
                        if(intentFrom.equals("ViewAnotherUserProfile"))
                        {
                            if(!TextUtils.isEmpty(retrieveUserName))
                            {
                                /* getting first name */
                                String arr[] = retrieveUserName.split(" ", 2);
                                getSupportActionBar().setTitle(arr[0]+"'s"+" Posts");
                            }
                        }
                    }
                    if(dataSnapshot.hasChild("userposition"))
                    {
                        retrieveUserPosition = dataSnapshot.child("userposition").getValue().toString();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




    private void DisplayUserAllPosts(final String userID)
    {
        Query currentUserPost = postDatabaseReference.orderByChild("userid").equalTo(userID);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserPosts>()
                .setQuery(currentUserPost, UserPosts.class)
                .build();

        FirebaseRecyclerAdapter<UserPosts, PostsViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserPosts, PostsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int i, @NonNull final UserPosts userPosts)
            {
                final String PostID = getRef(i).getKey();


                /* counting comments*/
                commentDatabaseReference.child(PostID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            long postCommentCount = dataSnapshot.getChildrenCount();
                            postsViewHolder.postcommentcount.setText(String.valueOf(postCommentCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


                /* getting current user vote details */
                voteDatabaseReference.child(PostID).child(currentUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            postsViewHolder.postBeforeVoteButton.setVisibility(View.GONE);
                            postsViewHolder.postAfterVoteButton.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            postsViewHolder.postBeforeVoteButton.setVisibility(View.VISIBLE);
                            postsViewHolder.postAfterVoteButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });



                /* getting votes count */
                voteDatabaseReference.child(PostID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            long postVoteCount = dataSnapshot.getChildrenCount();
                            postsViewHolder.postVoteCount.setText(String.valueOf(postVoteCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });



                /*  post description */
                postsViewHolder.postdescription.setVisibility(View.GONE);
                String tempPostDescription = userPosts.getPostdescription();
                if(!(TextUtils.isEmpty(tempPostDescription)))
                {
                    postsViewHolder.postdescription.setText(userPosts.getPostdescription());
                    postsViewHolder.postdescription.setVisibility(View.VISIBLE);
                }

                 /* post image */
                postsViewHolder.postfirstimage.setVisibility(View.GONE);
                final String tempPostImage = userPosts.getPostfirstimage();
                if(!(TextUtils.isEmpty(tempPostImage)))
                {
                    Picasso.with(PostActivity.this).load(userPosts.getPostfirstimage()).into(postsViewHolder.postfirstimage);
                    postsViewHolder.postfirstimage.setVisibility(View.VISIBLE);
                }

                /* count post images */
                final String tempPostSecondImage = userPosts.getPostsecondimage();
                final String tempPostThirdImage = userPosts.getPostthirdimage();
                if(!TextUtils.isEmpty(tempPostSecondImage) && !TextUtils.isEmpty(tempPostThirdImage))
                {
                    postsViewHolder.postimagecount.setText("+2");
                    postsViewHolder.postimagecount.setVisibility(View.VISIBLE);
                }
                if(!TextUtils.isEmpty(tempPostSecondImage) && TextUtils.isEmpty(tempPostThirdImage))
                {
                    postsViewHolder.postimagecount.setText("+1");
                    postsViewHolder.postimagecount.setVisibility(View.VISIBLE);
                }


                /* post date & time */
                postsViewHolder.postdate.setText(userPosts.getPostdate());
                postsViewHolder.posttime.setText(userPosts.getPosttime());


                //Picasso.with(PostActivity.this).load(R.drawable.default_user_image).into(postsViewHolder.postuserimage);
                if(!retrieveUserImage.isEmpty())
                {
                    Picasso.with(PostActivity.this).load(retrieveUserImage).placeholder(R.drawable.default_user_image).into(postsViewHolder.postuserimage);
                }


                if(!TextUtils.isEmpty(retrieveUserName))
                {
                    postsViewHolder.postusername.setText(retrieveUserName);
                }


                postsViewHolder.postuserposition.setVisibility(View.GONE);
                if(!TextUtils.isEmpty(retrieveUserPosition))
                {
                    postsViewHolder.postuserposition.setText(" • "+retrieveUserPosition);
                    postsViewHolder.postuserposition.setVisibility(View.VISIBLE);
                }


                postsViewHolder.menubtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OpenPostMenuDialog(PostID, userID);
                    }
                });


                postsViewHolder.postfirstimage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OpenViewImageDialog(tempPostImage, tempPostSecondImage, tempPostThirdImage);
                    }
                });


                postsViewHolder.postCommentButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent postCommentIntent = new Intent(PostActivity.this, PostCommentActivity.class);
                        postCommentIntent.putExtra("intentFrom", "PostActivity");
                        postCommentIntent.putExtra("intentPostID", PostID);
                        startActivity(postCommentIntent);
                    }
                });

                postsViewHolder.postBeforeVoteButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        postsViewHolder.postBeforeVoteButton.setVisibility(View.GONE);
                        postsViewHolder.postAfterVoteButton.setVisibility(View.VISIBLE);

                        HashMap voteMap = new HashMap();
                        voteMap.put(currentUserID,"true");
                        voteDatabaseReference.child(PostID).updateChildren(voteMap);

                    }
                });

                postsViewHolder.postAfterVoteButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        postsViewHolder.postBeforeVoteButton.setVisibility(View.VISIBLE);
                        postsViewHolder.postAfterVoteButton.setVisibility(View.GONE);

                        postsViewHolder.postVoteCount.setText(String.valueOf(Integer.parseInt(postsViewHolder.postVoteCount.getText().toString()) - 1 ));

                        voteDeleteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Votes").child(PostID).child(currentUserID);
                        voteDeleteDatabaseReference.removeValue();
                    }
                });

                postsViewHolder.postShareButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = userPosts.getPostdescription();
                        String shareSub = "Your subject here";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
                    }
                });
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                PostActivity.PostsViewHolder viewHolder = new PostActivity.PostsViewHolder(view);
                return viewHolder;
            }
        };

        postList.setAdapter(adapter);
        adapter.startListening();

    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView postusername, postuserposition,  posttime, postdate, postdescription, postimagecount, postcommentcount, postVoteCount;
        CircleImageView postuserimage;
        ImageView postfirstimage, menubtn, postCommentButton, postBeforeVoteButton, postAfterVoteButton, postShareButton;


        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            postusername = (TextView)itemView.findViewById(R.id.post_username);
            postuserposition = (TextView) itemView.findViewById(R.id.post_userposition);
            postuserimage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            posttime = (TextView)itemView.findViewById(R.id.post_time);
            postdate = (TextView)itemView.findViewById(R.id.post_date);
            postdescription = (TextView)itemView.findViewById(R.id.post_description);
            postfirstimage = (ImageView)itemView.findViewById(R.id.post_first_image);
            postimagecount = (TextView) itemView.findViewById(R.id.post_image_count);
            menubtn = (ImageView)itemView.findViewById(R.id.post_menu_button);
            postCommentButton = (ImageView) itemView.findViewById(R.id.post_comment_button);
            postcommentcount = (TextView) itemView.findViewById(R.id.post_comment_count);
            postBeforeVoteButton = (ImageView) itemView.findViewById(R.id.post_beforevote_button);
            postAfterVoteButton = (ImageView) itemView.findViewById(R.id.post_aftervote_button);
            postVoteCount = (TextView) itemView.findViewById(R.id.post_vote_count);
            postShareButton = (ImageView) itemView.findViewById(R.id.post_share_button);
        }
    }




    /* view images dialog activities */
    private void OpenViewImageDialog(final String tempPostFirstImage, final String tempPostSecondImage, String tempPostThirdImage)
    {
        final Dialog viewImagesDialog = new Dialog(this);
        viewImagesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        viewImagesDialog.setContentView(R.layout.view_photo_layout);
        viewImagesDialog.setTitle("View Images Window");
        viewImagesDialog.show();
        Window window = viewImagesDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final ProgressBar progressBar1 = viewImagesDialog.findViewById(R.id.viewphoto_progressBar1);
        /* adding progress bar while loading */
        progressBar1.setVisibility(View.VISIBLE);

        ImageView image1 = (ImageView) viewImagesDialog.findViewById(R.id.viewphoto_first_image);
        image1.setVisibility(View.VISIBLE);
        Picasso.with(this).load(tempPostFirstImage).into(image1, new Callback()
        {
            @Override
            public void onSuccess()
            {
                progressBar1.setVisibility(View.GONE);
            }

            @Override
            public void onError()
            {

            }
        });

        final ProgressBar progressBar2 = viewImagesDialog.findViewById(R.id.viewphoto_progressBar2);
        if(!TextUtils.isEmpty(tempPostSecondImage))
        {
            /* adding progress bar while loading */
            progressBar2.setVisibility(View.VISIBLE);

            ImageView image2 = (ImageView) viewImagesDialog.findViewById(R.id.viewphoto_second_image);
            image2.setVisibility(View.VISIBLE);
            Picasso.with(this).load(tempPostSecondImage).into(image2, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    progressBar2.setVisibility(View.GONE);
                }

                @Override
                public void onError()
                {

                }
            });
        }

        final ProgressBar progressBar3 = viewImagesDialog.findViewById(R.id.viewphoto_progressBar3);
        if(!TextUtils.isEmpty(tempPostThirdImage))
        {
            /* adding progress bar while loading */
            progressBar3.setVisibility(View.VISIBLE);

            ImageView image3 = (ImageView) viewImagesDialog.findViewById(R.id.viewphoto_third_image);
            image3.setVisibility(View.VISIBLE);
            Picasso.with(this).load(tempPostThirdImage).into(image3, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    progressBar3.setVisibility(View.GONE);
                }

                @Override
                public void onError()
                {

                }
            });
        }

        ImageView closebtn = (ImageView) viewImagesDialog.findViewById(R.id.viewphoto_close_button);
        closebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                viewImagesDialog.cancel();
            }
        });
    }





    private void OpenPostMenuDialog(final String postID, final String userID)
    {
        final Dialog postmenudialog = new Dialog(this);
        postmenudialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        postmenudialog.setContentView(R.layout.post_menu_layout);
        postmenudialog.setTitle("Post Menu Window");
        postmenudialog.show();

        RelativeLayout postdeletebtn = (RelativeLayout) postmenudialog.findViewById(R.id.delete_post_button);
        postdeletebtn.setEnabled(true);

        RelativeLayout posteditbtn = (RelativeLayout) postmenudialog.findViewById(R.id.edit_post_button);
        posteditbtn.setEnabled(true);

        RelativeLayout profileviewbtn = (RelativeLayout) postmenudialog.findViewById(R.id.view_profile_button);
        profileviewbtn.setVisibility(View.GONE);

        RelativeLayout viewCommentsBtn = postmenudialog.findViewById(R.id.view_comments_button);



        if(userID.equals(currentUserID))
        {

        }
        else
        {
            postdeletebtn.setVisibility(View.GONE);
            posteditbtn.setVisibility(View.GONE);
        }



        postdeletebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenDeleteConfirmDialog(postID);
                postmenudialog.cancel();
            }
        });


        posteditbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* user send to create post activity */
                Intent createpostIntent = new Intent(PostActivity.this, CreatePostActivity.class);
                createpostIntent.putExtra("intentFrom", "ProfileActivityToEditPost");
                createpostIntent.putExtra("intentPostID", postID);
                startActivity(createpostIntent);
                postmenudialog.cancel();
            }
        });

        viewCommentsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent postCommentIntent = new Intent(getApplication(), PostCommentActivity.class);
                postCommentIntent.putExtra("intentFrom", "FeedFragment");
                postCommentIntent.putExtra("intentPostID", postID);
                startActivity(postCommentIntent);
                postmenudialog.cancel();
            }
        });
    }



    private void OpenDeleteConfirmDialog(final String postID)
    {
        final Dialog deleteconfirmdialog = new Dialog(this);
        deleteconfirmdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteconfirmdialog.setContentView(R.layout.confirm_layout);
        deleteconfirmdialog.setTitle("Delete Confirm Dialog");
        deleteconfirmdialog.show();

        TextView title = (TextView)deleteconfirmdialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Delete Post");

        TextView description = (TextView)deleteconfirmdialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to delete post?");

        Button nobtn = (Button) deleteconfirmdialog.findViewById(R.id.confirm_dialog_no_button);
        nobtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteconfirmdialog.cancel();
            }
        });


        Button yesbtn = (Button) deleteconfirmdialog.findViewById(R.id.confirm_dialog_yes_button);
        yesbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                postDeleteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postID);
                postDeleteDatabaseReference.removeValue();
                postCommentDeleteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postID);
                postCommentDeleteDatabaseReference.removeValue();
                postVoteDeleteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Votes").child(postID);
                postVoteDeleteDatabaseReference.removeValue();
                deleteconfirmdialog.cancel();
                Toast.makeText(PostActivity.this,"Post deleted successfully",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void UserSendToCreatePostPage()
    {
        Intent createPostIntent = new Intent(PostActivity.this, CreatePostActivity.class);
        createPostIntent.putExtra("intentFrom", "ProfileActivityToCreatePost");
        startActivity(createPostIntent);
    }



    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
