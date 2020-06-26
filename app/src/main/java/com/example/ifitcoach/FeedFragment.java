package com.example.ifitcoach;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class FeedFragment extends Fragment
{
    private View feedview;
    private FloatingActionButton addNewPostbtn;

    private EditText searchBar;

    private RecyclerView postList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference postDatabaseReference, commentDatabaseReference, userDatabaseReference,
            postDeleteDatabaseReference, postCommentDeleteDatabaseReference, postVoteDeleteDatabaseReference,
            voteDatabaseReference, voteDeleteDatabaseReference;

    String currentUserID;


    public FeedFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        feedview = inflater.inflate(R.layout.fragment_feed, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        commentDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Comments");
        voteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Votes");


        postList = (RecyclerView) feedview.findViewById(R.id.post_list);
        postList.setNestedScrollingEnabled(false);
        postList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        /* define post order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        searchBar = (EditText) feedview.findViewById(R.id.feed_search_bar);
        searchBar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent findIntent = new Intent(getActivity(), FindActivity.class);
                findIntent.putExtra("intentPurpose", "FindUsers");
                startActivity(findIntent);
            }
        });


        addNewPostbtn = (FloatingActionButton) feedview.findViewById(R.id.feed_add_new_post_button);
        addNewPostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserSendToAddNewPostPage();
            }
        });


        return feedview;
    }




    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserPosts>()
                .setQuery(postDatabaseReference, UserPosts.class)
                .build();


        FirebaseRecyclerAdapter<UserPosts, PostsViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserPosts, PostsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int i, @NonNull final UserPosts userPosts)
            {
                /* getting post ID */
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


                /* setting the date and time */
                postsViewHolder.postDate.setText(userPosts.getPostdate());
                postsViewHolder.postTime.setText(userPosts.getPosttime());

                /* setting the post description */
                postsViewHolder.postDescription.setVisibility(View.GONE);
                String tempPostDescription = userPosts.getPostdescription();
                if(!TextUtils.isEmpty(tempPostDescription))
                {
                    postsViewHolder.postDescription.setText(userPosts.getPostdescription());
                    postsViewHolder.postDescription.setVisibility(View.VISIBLE);
                }

                /* setting the post image */
                postsViewHolder.postfirstimage.setVisibility(View.GONE);
                final String tempPostFirstImage = userPosts.getPostfirstimage();
                if(!TextUtils.isEmpty(tempPostFirstImage))
                {
                    Picasso.with(getContext()).load(userPosts.getPostfirstimage()).into(postsViewHolder.postfirstimage);
                    postsViewHolder.postfirstimage.setVisibility(View.VISIBLE);
                }

                /* count images */
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


                /* getting the user current updated details */
                final String PostUserID = userPosts.getUserid();
                if(!(TextUtils.isEmpty(PostUserID)))
                {
                    userDatabaseReference.child(PostUserID).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.exists())
                            {
                                Picasso.with(getContext()).load(R.drawable.default_user_image).into(postsViewHolder.postuserimage);
                                postsViewHolder.postUserPosition.setVisibility(View.GONE);


                                if (dataSnapshot.hasChild("username"))
                                {
                                    String name = dataSnapshot.child("username").getValue().toString();
                                    postsViewHolder.postUsername.setText(name);
                                }

                                if(dataSnapshot.hasChild("userimage"))
                                {
                                    String image = dataSnapshot.child("userimage").getValue().toString();
                                    Picasso.with(getContext()).load(image).placeholder(R.drawable.default_user_image).into(postsViewHolder.postuserimage);
                                }

                                if (dataSnapshot.hasChild("userposition"))
                                {
                                    String position = dataSnapshot.child("userposition").getValue().toString();
                                    postsViewHolder.postUserPosition.setText(" • "+position);
                                    postsViewHolder.postUserPosition.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    postsViewHolder.postMenubtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            OpenPostMenuDialog(PostID, PostUserID);
                        }
                    });


                    postsViewHolder.postuserimage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                            mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                            mainIntent.putExtra("intentUserID", PostUserID);
                            startActivity(mainIntent);
                        }
                    });


                    postsViewHolder.postUsername.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent profileIntent = new Intent(getActivity(), MainActivity.class);
                            profileIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                            profileIntent.putExtra("intentUserID", PostUserID);
                            startActivity(profileIntent);
                        }
                    });

                    postsViewHolder.postfirstimage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            OpenViewImageDialog(tempPostFirstImage, tempPostSecondImage, tempPostThirdImage);
                        }
                    });

                    postsViewHolder.postCommentButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent postCommentIntent = new Intent(getActivity(), PostCommentActivity.class);
                            postCommentIntent.putExtra("intentFrom", "FeedFragment");
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


            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };

        postList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView postUsername, postUserPosition, postTime, postDate, postDescription, postimagecount, postcommentcount, postVoteCount;
        CircleImageView postuserimage;
        ImageView postfirstimage, postMenubtn, postCommentButton, postBeforeVoteButton, postAfterVoteButton, postShareButton;



        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            postUsername = (TextView)itemView.findViewById(R.id.post_username);
            postUserPosition = (TextView) itemView.findViewById(R.id.post_userposition);
            postuserimage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            postTime = (TextView)itemView.findViewById(R.id.post_time);
            postDate = (TextView)itemView.findViewById(R.id.post_date);
            postDescription = (TextView)itemView.findViewById(R.id.post_description);
            postfirstimage = (ImageView)itemView.findViewById(R.id.post_first_image);
            postimagecount = (TextView) itemView.findViewById(R.id.post_image_count);
            postMenubtn = (ImageView)itemView.findViewById(R.id.post_menu_button);
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
        final Dialog viewImagesDialog = new Dialog(getContext());
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
        Picasso.with(getContext()).load(tempPostFirstImage).into(image1, new Callback()
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
            Picasso.with(getContext()).load(tempPostSecondImage).into(image2, new Callback()
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
            Picasso.with(getContext()).load(tempPostThirdImage).into(image3, new Callback()
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




    private void OpenPostMenuDialog(final String postID, final String postUserID)
    {
        final Dialog postmenudialog = new Dialog(getContext());
        postmenudialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        postmenudialog.setContentView(R.layout.post_menu_layout);
        postmenudialog.setTitle("Post Menu Window");
        postmenudialog.show();

        RelativeLayout postdeletebtn = (RelativeLayout) postmenudialog.findViewById(R.id.delete_post_button);
        postdeletebtn.setEnabled(true);
        postdeletebtn.setVisibility(View.GONE);

        RelativeLayout posteditbtn = (RelativeLayout) postmenudialog.findViewById(R.id.edit_post_button);
        posteditbtn.setEnabled(true);
        posteditbtn.setVisibility(View.GONE);

        RelativeLayout viewprofilebtn = (RelativeLayout) postmenudialog.findViewById(R.id.view_profile_button);
        viewprofilebtn.setEnabled(true);

        RelativeLayout viewCommentsBtn = postmenudialog.findViewById(R.id.view_comments_button);


        if(currentUserID.equals(postUserID))
        {
            postdeletebtn.setVisibility(View.VISIBLE);
            posteditbtn.setVisibility(View.VISIBLE);
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

        viewprofilebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* user send to profile activity */
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                mainIntent.putExtra("intentUserID", postUserID);
                startActivity(mainIntent);
                postmenudialog.cancel();
            }
        });

        posteditbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* user send to create post activity */
                Intent createpostIntent = new Intent(getActivity(), CreatePostActivity.class);
                createpostIntent.putExtra("intentFrom", "FeedFragmentToEditPost");
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
                Intent postCommentIntent = new Intent(getActivity(), PostCommentActivity.class);
                postCommentIntent.putExtra("intentFrom", "FeedFragment");
                postCommentIntent.putExtra("intentPostID", postID);
                startActivity(postCommentIntent);
                postmenudialog.cancel();
            }
        });

    }



    private void OpenDeleteConfirmDialog(final String postID)
    {
        final Dialog deleteconfirmdialog = new Dialog(getContext());
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
                Toast.makeText(getActivity(),"Post deleted successfully",Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void UserSendToAddNewPostPage()
    {
        Intent createPostIntent = new Intent(getActivity(), CreatePostActivity.class);
        createPostIntent.putExtra("intentFrom", "FeedFragmentToCreatePost");
        startActivity(createPostIntent);
    }

}
