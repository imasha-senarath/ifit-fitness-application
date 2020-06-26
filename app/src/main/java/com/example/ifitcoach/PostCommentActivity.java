package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference commentDatabaseReference, commentDeleteDatabaseReference, userDatabaseReference;

    private RecyclerView commentList;

    private EditText userComment;
    private ImageButton sendButton;


    String currentUserID;

    String intentFrom, intentPostID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);

        /* Adding tool bar & title to post comment activity and hide user image and notification icon */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("FeedFragment") || intentFrom.equals("PostActivity"))
        {
            intentPostID = intent.getExtras().getString("intentPostID");
        }


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        commentDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(intentPostID);
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        userComment = (EditText) findViewById(R.id.post_comment_input);
        sendButton = (ImageButton) findViewById(R.id.post_comment_send_button);

        userComment.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(userComment, InputMethodManager.SHOW_IMPLICIT);


        commentList = (RecyclerView) findViewById(R.id.comments_list);
        commentList.setNestedScrollingEnabled(false);
        commentList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define comment order*/
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);


        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddUserComment();
            }
        });

        LoadPostComments();
    }


    private void LoadPostComments()
    {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<PostComments>()
                .setQuery(commentDatabaseReference, PostComments.class)
                .build();

        FirebaseRecyclerAdapter<PostComments,CommentViewHolder> adapter
                = new FirebaseRecyclerAdapter<PostComments, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder commentViewHolder, int i, @NonNull final PostComments postComments)
            {
                /* get comment ID */
                final String commentID = getRef(i).getKey();

                commentViewHolder.commentDescription.setText(postComments.getCommentdescription());
                commentViewHolder.commentDate.setText(postComments.getCommentdate());
                commentViewHolder.commentTime.setText(postComments.getCommenttime());

                if(postComments.getUserid().equals(currentUserID))
                {
                    commentViewHolder.commentDeleteButton.setVisibility(View.VISIBLE);
                }

                if(!TextUtils.isEmpty(postComments.getUserid()))
                {
                    userDatabaseReference.child(postComments.getUserid()).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                if (dataSnapshot.hasChild("username"))
                                {
                                    String name = dataSnapshot.child("username").getValue().toString();
                                    commentViewHolder.userName.setText(name);
                                }

                                if(dataSnapshot.hasChild("userimage"))
                                {
                                    String image = dataSnapshot.child("userimage").getValue().toString();
                                    Picasso.with(PostCommentActivity.this).load(image).placeholder(R.drawable.default_user_image).into(commentViewHolder.userImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }

                commentViewHolder.commentDeleteButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OpenDeleteConfirmDialog(commentID);
                    }
                });

                commentViewHolder.userImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent mainIntent = new Intent(PostCommentActivity.this, MainActivity.class);
                        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                        mainIntent.putExtra("intentUserID", postComments.getUserid());
                        startActivity(mainIntent);
                    }
                });

                commentViewHolder.userName.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent mainIntent = new Intent(PostCommentActivity.this, MainActivity.class);
                        mainIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
                        mainIntent.putExtra("intentUserID", postComments.getUserid());
                        startActivity(mainIntent);
                    }
                });
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout,parent,false);
                CommentViewHolder commentViewHolder = new CommentViewHolder(view);
                return commentViewHolder;
            }
        };
        commentList.setAdapter(adapter);
        adapter.startListening();
    }



    private void OpenDeleteConfirmDialog(final String commentID)
    {
        final Dialog deleteConfirmDialog = new Dialog(this);
        deleteConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteConfirmDialog.setContentView(R.layout.confirm_layout);
        deleteConfirmDialog.setTitle("Delete Confirm Dialog");
        deleteConfirmDialog.show();

        TextView title = (TextView)deleteConfirmDialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Delete Comment");

        TextView description = (TextView)deleteConfirmDialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to delete comment?");

        Button nobtn = (Button) deleteConfirmDialog.findViewById(R.id.confirm_dialog_no_button);
        nobtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteConfirmDialog.cancel();
            }
        });


        Button yesbtn = (Button) deleteConfirmDialog.findViewById(R.id.confirm_dialog_yes_button);
        yesbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                commentDeleteDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(intentPostID).child(commentID);
                commentDeleteDatabaseReference.removeValue();
                deleteConfirmDialog.cancel();
                Toast.makeText(PostCommentActivity.this,"comment deleted successfully",Toast.LENGTH_SHORT).show();
            }
        });
    }



    public static class CommentViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, commentTime, commentDate, commentDescription;
        CircleImageView userImage;
        ImageButton commentDeleteButton;

        public CommentViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userImage = (CircleImageView) itemView.findViewById(R.id.comment_profile_image);
            userName = (TextView) itemView.findViewById(R.id.comment_username);
            commentDescription = (TextView) itemView.findViewById(R.id.comment_description);
            commentDate = (TextView) itemView.findViewById(R.id.comment_date);
            commentTime = (TextView) itemView.findViewById(R.id.comment_time);
            commentDeleteButton = (ImageButton)itemView.findViewById(R.id.comment_delete_button);
        }
    }


    private void AddUserComment()
    {
        if(!TextUtils.isEmpty(userComment.getText().toString()))
        {
            /* get current date */
            Calendar calendar4 = Calendar.getInstance();
            SimpleDateFormat currenDate = new SimpleDateFormat("dd MMM yyyy");
            String currentDate = currenDate.format(calendar4.getTime());

            /* get current time */
            Calendar calendar3 = Calendar.getInstance();
            SimpleDateFormat currenTime = new SimpleDateFormat("hh:mm aa");
            String currentTime = currenTime.format(calendar3.getTime());

            HashMap commentMap = new HashMap();
            commentMap.put("userid",currentUserID);
            commentMap.put("commentdescription",userComment.getText().toString());
            commentMap.put("commentdate",currentDate);
            commentMap.put("commenttime",currentTime);


            /* get current date */
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat currenDate2 = new SimpleDateFormat("yyyyMMdd");
            String date = currenDate2.format(calendar1.getTime());

            /* get current time */
            Calendar calendar2 = Calendar.getInstance();
            SimpleDateFormat currenTime2 = new SimpleDateFormat("HHmmss");
            String time = currenTime2.format(calendar2.getTime());

            String commentRandomName = date + time;

            commentDatabaseReference.child(commentRandomName + currentUserID).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        userComment.setText("");
                    }
                    else
                    {
                        String msg = task.getException().getMessage();
                        Toast.makeText(PostCommentActivity.this, "Error : "+msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
