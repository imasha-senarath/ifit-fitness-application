package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddWorkoutActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference workoutDiaryDatabaseReference;

    private TextView workoutName, workoutCalories, workoutDuration;
    private ImageButton durationMinusBtn, durationAddBtn;
    private Button addBtn;

    private ImageView workoutImage;

    private String totalCalories="0",totalDuration="0", calories="0", duration="0";

    private String currentUserID, intentFrom, currentDate;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        /* Adding tool bar & title to */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);

        /* getting current date */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenDate = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = currenDate.format(calendar.getTime());


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();


        workoutImage = (ImageView) findViewById(R.id.add_workout_image);
        workoutName = (TextView) findViewById(R.id.add_workout_name);
        workoutCalories = (TextView) findViewById(R.id.add_workout_calories);
        workoutDuration = (TextView) findViewById(R.id.add_workout_duration);
        durationMinusBtn = (ImageButton) findViewById(R.id.add_workout_duration_minus_button);
        durationAddBtn = (ImageButton) findViewById(R.id.add_workout_duration_add_button);
        addBtn = (Button) findViewById(R.id.add_workout_add_button);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(!intentFrom.isEmpty())
        {
            workoutDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WorkoutDiaries")
                    .child(currentUserID).child(currentDate).child(intentFrom);

            RetrieveTotalCalories();
            LoadAddWorkoutPage(intentFrom);

            addBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AddUserWorkoutToDiary();
                }
            });
        }
    }



    /*getting current calories data*/
    private void RetrieveTotalCalories()
    {
        workoutDiaryDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("calories"))
                    {
                        totalCalories = dataSnapshot.child("calories").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("duration"))
                    {
                        totalDuration = dataSnapshot.child("duration").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




    private void LoadAddWorkoutPage(String workoutType)
    {
        /* ## Walking ## */
        if(workoutType.equals("Walking"))
        {
            calories = "4";
            duration = "1";

            workoutImage.setImageDrawable(null);
            workoutImage.setBackgroundResource(R.drawable.walking_icon);
            workoutName.setText("Walking");
            workoutCalories.setText(calories+" Calories");
            workoutDuration.setText(duration+" min");

            durationMinusBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(Integer.parseInt(duration) > 1)
                    {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        calories = String.valueOf(Integer.parseInt(calories) - 4);
                        workoutDuration.setText(duration+" min");
                        workoutCalories.setText(calories+" Calories");
                    }
                }
            });

            durationAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    duration = String.valueOf(Integer.parseInt(duration) + 1);
                    calories = String.valueOf(Integer.parseInt(calories) + 4);
                    workoutDuration.setText(duration+" min");
                    workoutCalories.setText(calories+" Calories");
                }
            });
        }


        /* ## Running ## */
        if(workoutType.equals("Running"))
        {
            calories = "9";
            duration = "1";

            workoutImage.setImageDrawable(null);
            workoutImage.setBackgroundResource(R.drawable.running_icon);
            workoutName.setText("Running");
            workoutCalories.setText(calories+" Calories");
            workoutDuration.setText(duration+" min");

            durationMinusBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(Integer.parseInt(duration) > 1)
                    {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        calories = String.valueOf(Integer.parseInt(calories) - 9);
                        workoutDuration.setText(duration+" min");
                        workoutCalories.setText(calories+" Calories");
                    }
                }
            });

            durationAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    duration = String.valueOf(Integer.parseInt(duration) + 1);
                    calories = String.valueOf(Integer.parseInt(calories) + 9);
                    workoutDuration.setText(duration+" min");
                    workoutCalories.setText(calories+" Calories");
                }
            });
        }


        /* ## Cycling ## */
        if(workoutType.equals("Cycling"))
        {
            calories = "7";
            duration = "1";

            workoutImage.setImageDrawable(null);
            workoutImage.setBackgroundResource(R.drawable.cycling_icon);
            workoutName.setText("Cycling");
            workoutCalories.setText(calories+" Calories");
            workoutDuration.setText(duration+" min");

            durationMinusBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(Integer.parseInt(duration) > 1)
                    {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        calories = String.valueOf(Integer.parseInt(calories) - 7);
                        workoutDuration.setText(duration+" min");
                        workoutCalories.setText(calories+" Calories");
                    }
                }
            });

            durationAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    duration = String.valueOf(Integer.parseInt(duration) + 1);
                    calories = String.valueOf(Integer.parseInt(calories) + 7);
                    workoutDuration.setText(duration+" min");
                    workoutCalories.setText(calories+" Calories");
                }
            });
        }


        /* ## Hiking ## */
        if(workoutType.equals("Hiking"))
        {
            calories = "6";
            duration = "1";

            workoutImage.setImageDrawable(null);
            workoutImage.setBackgroundResource(R.drawable.hiking_icon);
            workoutName.setText("Hiking");
            workoutCalories.setText(calories+" Calories");
            workoutDuration.setText(duration+" min");

            durationMinusBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(Integer.parseInt(duration) > 1)
                    {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        calories = String.valueOf(Integer.parseInt(calories) - 6);
                        workoutDuration.setText(duration+" min");
                        workoutCalories.setText(calories+" Calories");
                    }
                }
            });

            durationAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    duration = String.valueOf(Integer.parseInt(duration) + 1);
                    calories = String.valueOf(Integer.parseInt(calories) + 6);
                    workoutDuration.setText(duration+" min");
                    workoutCalories.setText(calories+" Calories");
                }
            });
        }

        /* ## Swimming ## */
        if(workoutType.equals("Swimming"))
        {
            calories = "11";
            duration = "1";

            workoutImage.setImageDrawable(null);
            workoutImage.setBackgroundResource(R.drawable.swimming_icon);
            workoutName.setText("Swimming");
            workoutCalories.setText(calories+" Calories");
            workoutDuration.setText(duration+" min");

            durationMinusBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(Integer.parseInt(duration) > 1)
                    {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        calories = String.valueOf(Integer.parseInt(calories) - 11);
                        workoutDuration.setText(duration+" min");
                        workoutCalories.setText(calories+" Calories");
                    }
                }
            });

            durationAddBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    duration = String.valueOf(Integer.parseInt(duration) + 1);
                    calories = String.valueOf(Integer.parseInt(calories) + 11);
                    workoutDuration.setText(duration+" min");
                    workoutCalories.setText(calories+" Calories");
                }
            });
        }

    }


    private void AddUserWorkoutToDiary()
    {
        loadingBar = new ProgressDialog(this);
        String ProgressDialogMessage="Adding Workout...";
        SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
        spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
        loadingBar.setMessage(spannableMessage);
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setCancelable(false);


        totalCalories = String.valueOf(Integer.parseInt(totalCalories) + Integer.parseInt(calories));
        totalDuration = String.valueOf(Integer.parseInt(totalDuration) + Integer.parseInt(duration));


        HashMap workoutMap = new HashMap();
        workoutMap.put("calories", totalCalories);
        workoutMap.put("duration", totalDuration);

        workoutDiaryDatabaseReference.updateChildren(workoutMap).addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    loadingBar.cancel();
                    UserSendToDiaryPage();
                }
                else
                {
                    loadingBar.dismiss();
                    String msg = task.getException().getMessage();
                    Toast.makeText(AddWorkoutActivity.this, "Error"+msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UserSendToDiaryPage()
    {
        Intent diaryIntent = new Intent(AddWorkoutActivity.this, DiaryActivity.class);
        diaryIntent.putExtra("intentFrom", "AddFoodActivity");
        startActivity(diaryIntent);
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
