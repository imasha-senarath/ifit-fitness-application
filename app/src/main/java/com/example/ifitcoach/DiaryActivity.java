package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiaryActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference foodDiaryDatabaseReference, foodDatabaseReference, userDatabaseReference, waterDiaryDatabaseReference,
            workoutDiaryDatabaseReference;


    private String strUserCurrentWeight, strUserByear, strUserAge, strUserHeight, strUserGender, strUserActivityLevel,
            strUserFood="0", strUserCarbs="0.0", strUserFat="0.0", strUserProtein="0.0", strUserWorkout="0", userServingSize="1",
            strUserDailyCalorieIntake, strUserCaloriesRemaining;

    private String breakfastNumberOfServings = "1", breakfastServingSize = "1", lunchNumberOfServings = "1", lunchServingSize = "1",
            dinnerNumberOfServings = "1", dinnerServingSize = "1", snackNumberOfServings = "1", snackServingSize = "1";

    private LinearLayout dateButton;
    private TextView diaryDate;

    private TextView userCaloriesRemaining, userFoodCalories, userWorkoutCalories, userDailyCaloriesIntake, userCarbs, userFat, userProtein;

    private RecyclerView userBreakfastFoodList, userLunchFoodList, userDinnerFoodList, userSnacksFoodList, userWorkoutList;

    private TextView waterGlasses;
    private LinearLayout warerGlassesContainer;

    private String currentUserID;

    private long diariesFoodCount, diariesWorkoutCount;

    private String intentFrom, intentUserID;

    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("ViewAnotherUserProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        /* Adding tool bar & title to diary activity and hiding user image */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Diary");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        /* getting current date */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenDate = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = currenDate.format(calendar.getTime());


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        foodDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("FoodDiaries");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        waterDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WaterDiaries");
        workoutDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WorkoutDiaries");


        dateButton = (LinearLayout) findViewById(R.id.diary_date_button);
        diaryDate = (TextView) findViewById(R.id.diary_date);


        userCaloriesRemaining = (TextView) findViewById(R.id.diary_caloriecard_calorieremaining);
        userDailyCaloriesIntake = (TextView) findViewById(R.id.diary_caloriecard_dailycalorieintake);
        userFoodCalories = (TextView) findViewById(R.id.diary_caloriecard_foodcalorie);
        userWorkoutCalories = (TextView) findViewById(R.id.diary_caloriecard_workoutcalories);


        userCarbs = (TextView) findViewById(R.id.diary_summary_card_carbs);
        userFat = (TextView) findViewById(R.id.diary_summary_card_fat);
        userProtein = (TextView) findViewById(R.id.diary_summary_card_protein);


        userBreakfastFoodList = (RecyclerView)findViewById(R.id.diary_breakfast_foodlist);
        userBreakfastFoodList.setNestedScrollingEnabled(false);

        userLunchFoodList = (RecyclerView)findViewById(R.id.diary_lunch_foodlist);
        userLunchFoodList.setNestedScrollingEnabled(false);

        userDinnerFoodList = (RecyclerView)findViewById(R.id.diary_dinner_foodlist);
        userDinnerFoodList.setNestedScrollingEnabled(false);

        userSnacksFoodList = (RecyclerView)findViewById(R.id.diary_snack_foodlist);
        userSnacksFoodList.setNestedScrollingEnabled(false);

        userWorkoutList = (RecyclerView)findViewById(R.id.diary_workout_list);
        userWorkoutList.setNestedScrollingEnabled(false);

        waterGlasses = (TextView) findViewById(R.id.diary_water_glasses);
        warerGlassesContainer = (LinearLayout) findViewById(R.id.diary_water_glasses_container);


        LinearLayoutManager breakfastLinearLayoutManager = new LinearLayoutManager(this);
        breakfastLinearLayoutManager.setReverseLayout(true);
        breakfastLinearLayoutManager.setStackFromEnd(true);
        userBreakfastFoodList.setLayoutManager(breakfastLinearLayoutManager);


        LinearLayoutManager lunchLinearLayoutManager = new LinearLayoutManager(this);
        lunchLinearLayoutManager.setReverseLayout(true);
        lunchLinearLayoutManager.setStackFromEnd(true);
        userLunchFoodList.setLayoutManager(lunchLinearLayoutManager);

        LinearLayoutManager dinnerLinearLayoutManager = new LinearLayoutManager(this);
        dinnerLinearLayoutManager.setReverseLayout(true);
        dinnerLinearLayoutManager.setStackFromEnd(true);
        userDinnerFoodList.setLayoutManager(dinnerLinearLayoutManager);

        LinearLayoutManager snacksLinearLayoutManager = new LinearLayoutManager(this);
        snacksLinearLayoutManager.setReverseLayout(true);
        snacksLinearLayoutManager.setStackFromEnd(true);
        userSnacksFoodList.setLayoutManager(snacksLinearLayoutManager);

        LinearLayoutManager workoutLinearLayoutManager = new LinearLayoutManager(this);
        workoutLinearLayoutManager.setReverseLayout(true);
        workoutLinearLayoutManager.setStackFromEnd(true);
        userWorkoutList.setLayoutManager(workoutLinearLayoutManager);


        dateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupCalendar();
            }
        });


        if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            GetUserData(intentUserID, currentDate);
            DisplayUserAllBreakfastFoods(intentUserID, currentDate);
            DisplayUserAllLunchFoods(intentUserID, currentDate);
            DisplayUserAllDinnerFoods(intentUserID, currentDate);
            DisplayUserAllSnacks(intentUserID, currentDate);
            DisplayUserAllWorkouts(intentUserID, currentDate);
            DisplayUserWaterGlasses(intentUserID, currentDate);
        }
        else
        {
            GetUserData(currentUserID, currentDate);
            DisplayUserAllBreakfastFoods(currentUserID, currentDate);
            DisplayUserAllLunchFoods(currentUserID, currentDate);
            DisplayUserAllDinnerFoods(currentUserID, currentDate);
            DisplayUserAllSnacks(currentUserID, currentDate);
            DisplayUserAllWorkouts(currentUserID, currentDate);
            DisplayUserWaterGlasses(currentUserID, currentDate);
        }
    }



    /* open date picker dialog */
    private void PopupCalendar()
    {
        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                /* date formats*/
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);

                String diaryChangedDate;

                /* getting the chosen date */
                diaryChangedDate = dayFormat.format(myCalendar.getTime()) +" "+ monthFormat.format(myCalendar.getTime()) +" "+ yearFormat.format(myCalendar.getTime());
                diaryDate.setText(diaryChangedDate);
                diaryChangedDate =  diaryChangedDate.replace(" ","-");


                if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
                {
                    GetUserData(intentUserID, diaryChangedDate);
                    DisplayUserAllBreakfastFoods(intentUserID, diaryChangedDate);
                    DisplayUserAllLunchFoods(intentUserID, diaryChangedDate);
                    DisplayUserAllDinnerFoods(intentUserID, diaryChangedDate);
                    DisplayUserAllSnacks(intentUserID, diaryChangedDate);
                    DisplayUserAllWorkouts(intentUserID, diaryChangedDate);
                    DisplayUserWaterGlasses(intentUserID, diaryChangedDate);
                }
                else
                {
                    GetUserData(currentUserID, diaryChangedDate);
                    DisplayUserAllBreakfastFoods(currentUserID, diaryChangedDate);
                    DisplayUserAllLunchFoods(currentUserID, diaryChangedDate);
                    DisplayUserAllDinnerFoods(currentUserID, diaryChangedDate);
                    DisplayUserAllSnacks(currentUserID, diaryChangedDate);
                    DisplayUserAllWorkouts(currentUserID, diaryChangedDate);
                    DisplayUserWaterGlasses(currentUserID, diaryChangedDate);
                }
            }
        };

        /* set today date */
        new DatePickerDialog(this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),  myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }




    public void GetUserData(final String userID, final String date)
    {
        userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
                    {
                        if(dataSnapshot.hasChild("username"))
                        {
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            if(!TextUtils.isEmpty(retrieveUserName))
                            {
                                /* getting first name of the user */
                                String arr[] = retrieveUserName.split(" ", 2);
                                getSupportActionBar().setTitle(arr[0]+"'s"+" Diary");
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("usercurrentweight"))
                    {
                        strUserCurrentWeight = dataSnapshot.child("usercurrentweight").getValue().toString();
                    }


                    if(dataSnapshot.hasChild("userheight"))
                    {
                        strUserHeight = dataSnapshot.child("userheight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userbyear"))
                    {
                        strUserByear = dataSnapshot.child("userbyear").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("usergender"))
                    {
                        strUserGender = dataSnapshot.child("usergender").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("useractivitylevel"))
                    {
                        strUserActivityLevel = dataSnapshot.child("useractivitylevel").getValue().toString();
                    }

                    GetUserFoodData(userID, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void GetUserFoodData(final String userID, final String date)
    {
        foodDiaryDatabaseReference.child(userID).child(date).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    /* counting the user foods */
                    diariesFoodCount = dataSnapshot.getChildrenCount();

                    for(final DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        /* getting the food ID */
                        String foodID = ds.getKey();

                        foodDiaryDatabaseReference.child(userID).child(date).child(foodID).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("numberOfServing"))
                                    {
                                        userServingSize = dataSnapshot.child("numberOfServing").getValue().toString();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });


                        foodDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    String servingSize="1";

                                    if(dataSnapshot.hasChild("foodservingsize"))
                                    {
                                       servingSize = dataSnapshot.child("foodservingsize").getValue().toString();
                                    }

                                    if(dataSnapshot.hasChild("foodcalories"))
                                    {
                                        String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                        foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(userServingSize) * Double.parseDouble(foodCalories)) / Double.parseDouble(servingSize));
                                        strUserFood = String.format(Locale.US,"%.0f",(Double.parseDouble(strUserFood) + Double.parseDouble(foodCalories)));
                                    }

                                    if(dataSnapshot.hasChild("foodcarbs"))
                                    {
                                        String foodCarbs = dataSnapshot.child("foodcarbs").getValue().toString();
                                        foodCarbs = String.format(Locale.US,"%.1f",(Double.parseDouble(userServingSize) * Double.parseDouble(foodCarbs)) / Double.parseDouble(servingSize));
                                        strUserCarbs = String.format(Locale.US,"%.1f",(Double.parseDouble(strUserCarbs) + Double.parseDouble(foodCarbs)));
                                    }

                                    if(dataSnapshot.hasChild("foodprotein"))
                                    {
                                        String foodProtein = dataSnapshot.child("foodprotein").getValue().toString();
                                        foodProtein = String.format(Locale.US,"%.1f",(Double.parseDouble(userServingSize) * Double.parseDouble(foodProtein)) / Double.parseDouble(servingSize));
                                        strUserProtein = String.format(Locale.US,"%.1f",(Double.parseDouble(strUserProtein) + Double.parseDouble(foodProtein)));

                                    }

                                    if(dataSnapshot.hasChild("foodfat"))
                                    {
                                        String foodFat = dataSnapshot.child("foodfat").getValue().toString();
                                        foodFat = String.format(Locale.US,"%.1f",(Double.parseDouble(userServingSize) * Double.parseDouble(foodFat)) / Double.parseDouble(servingSize));
                                        strUserFat = String.format(Locale.US,"%.1f",(Double.parseDouble(strUserFat) + Double.parseDouble(foodFat)));
                                    }

                                    servingSize="1";
                                    userServingSize="1";

                                    diariesFoodCount = diariesFoodCount - 1;

                                    /* checking whether  finished the calculation or not */
                                    if(diariesFoodCount == 0)
                                    {
                                        GetUserWorkoutData(userID, date);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });
                    }
                }
                else
                {
                    GetUserWorkoutData(userID, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void GetUserWorkoutData(final String userID, final String date)
    {
        workoutDiaryDatabaseReference.child(userID).child(date).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    /* counting the user workout */
                    diariesWorkoutCount = dataSnapshot.getChildrenCount();

                    for(final DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        /* getting workout type */
                        String workoutType = ds.getKey();

                        workoutDiaryDatabaseReference.child(userID).child(date).child(workoutType).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.hasChild("calories"))
                                {
                                    String workoutCalories = dataSnapshot.child("calories").getValue().toString();
                                    strUserWorkout = String.valueOf(Integer.parseInt(strUserWorkout) + Integer.parseInt(workoutCalories));
                                }

                                diariesWorkoutCount = diariesWorkoutCount - 1;

                                /* checking whether  finished the calculation or not */
                                if(diariesWorkoutCount == 0)
                                {
                                    SetSummaryCardsDetails();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });


                    }
                }
                else
                {
                    SetSummaryCardsDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void SetSummaryCardsDetails()
    {
        FormulaCalculations fc = new FormulaCalculations();

        if (!TextUtils.isEmpty(strUserCurrentWeight) && !TextUtils.isEmpty(strUserByear) &&
                !TextUtils.isEmpty(strUserHeight) && !TextUtils.isEmpty(strUserActivityLevel) &&
                !TextUtils.isEmpty(strUserGender))
        {

            strUserAge = fc.Age(strUserByear);

            strUserDailyCalorieIntake = fc.TDEE(strUserAge, strUserCurrentWeight, strUserHeight, strUserGender, strUserActivityLevel);

        }
        else
        {
            strUserDailyCalorieIntake="2300";
        }

        userDailyCaloriesIntake.setText(strUserDailyCalorieIntake);

        userFoodCalories.setText(strUserFood);
        userWorkoutCalories.setText(strUserWorkout);
        userCarbs.setText(strUserCarbs+"g");
        userFat.setText(strUserFat+"g");
        userProtein.setText(strUserProtein+"g");

        strUserCaloriesRemaining = fc.CaloriesRemaining(strUserDailyCalorieIntake, strUserFood, strUserWorkout);
        userCaloriesRemaining.setText(strUserCaloriesRemaining);

        if(Integer.parseInt(strUserCaloriesRemaining) < 0)
        {
            userCaloriesRemaining.setTextColor(getResources().getColor(R.color.WarningTextColor));
        }

        strUserFood="0";
        strUserCarbs="0";
        strUserFat="0";
        strUserProtein="0";
        strUserWorkout="0";
    }


    private void DisplayUserAllBreakfastFoods(final String userID, final String date)
    {

        Query userBreakfast = foodDiaryDatabaseReference.child(userID).child(date).orderByChild("foodType").equalTo("breakfast");

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                .setQuery(userBreakfast, Foods.class)
                .build();

        FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String foodID = getRef(i).getKey();
                        if(!TextUtils.isEmpty(foodID))
                        {

                            foodDiaryDatabaseReference.child(userID).child(date).child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("numberOfServing"))
                                        {
                                            breakfastNumberOfServings = dataSnapshot.child("numberOfServing").getValue().toString();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            foodDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("foodservingsize"))
                                        {
                                            breakfastServingSize = dataSnapshot.child("foodservingsize").getValue().toString();
                                        }

                                        if(dataSnapshot.hasChild("foodname"))
                                        {
                                            String foodName = dataSnapshot.child("foodname").getValue().toString();
                                            userFoodsViewHolder.layoutfoodname.setText(foodName);
                                        }

                                        if(dataSnapshot.hasChild("foodcalories"))
                                        {
                                            String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                            foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(breakfastNumberOfServings) * Double.parseDouble(foodCalories)) / Double.parseDouble(breakfastServingSize));
                                            userFoodsViewHolder.layoutfoodcalorie.setText(foodCalories);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            breakfastNumberOfServings = "1";
                            breakfastServingSize = "1";
                        }

                    }

                    @NonNull
                    @Override
                    public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                        UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                        return userFoodsViewHolder;
                    }
                };

        userBreakfastFoodList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }



    private void DisplayUserAllLunchFoods(final String userID, final String date)
    {

        Query userLunch = foodDiaryDatabaseReference.child(userID).child(date).orderByChild("foodType").equalTo("lunch");


        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(userLunch, Foods.class)
                        .build();

        FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String foodID = getRef(i).getKey();
                        if(!TextUtils.isEmpty(foodID))
                        {
                            foodDiaryDatabaseReference.child(userID).child(date).child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("numberOfServing"))
                                        {
                                            lunchNumberOfServings = dataSnapshot.child("numberOfServing").getValue().toString();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            foodDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("foodservingsize"))
                                        {
                                            lunchServingSize = dataSnapshot.child("foodservingsize").getValue().toString();
                                        }

                                        if(dataSnapshot.hasChild("foodname"))
                                        {
                                            String foodName = dataSnapshot.child("foodname").getValue().toString();
                                            userFoodsViewHolder.layoutfoodname.setText(foodName);
                                        }
                                        if(dataSnapshot.hasChild("foodcalories"))
                                        {
                                            String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                            foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(lunchNumberOfServings) * Double.parseDouble(foodCalories)) / Double.parseDouble(lunchServingSize));
                                            userFoodsViewHolder.layoutfoodcalorie.setText(foodCalories);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            lunchNumberOfServings = "1";
                            lunchServingSize = "1";
                        }
                    }

                    @NonNull
                    @Override
                    public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                        UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                        return userFoodsViewHolder;
                    }
                };
        userLunchFoodList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    private void DisplayUserAllDinnerFoods(final String userID, final String date)
    {
        Query userDinner = foodDiaryDatabaseReference.child(userID).child(date).orderByChild("foodType").equalTo("dinner");

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(userDinner, Foods.class)
                        .build();

        FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String foodID = getRef(i).getKey();
                        if(!TextUtils.isEmpty(foodID))
                        {
                            foodDiaryDatabaseReference.child(userID).child(date).child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("numberOfServing"))
                                        {
                                            dinnerNumberOfServings = dataSnapshot.child("numberOfServing").getValue().toString();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            foodDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("foodservingsize"))
                                        {
                                            dinnerServingSize = dataSnapshot.child("foodservingsize").getValue().toString();
                                        }

                                        if(dataSnapshot.hasChild("foodname"))
                                        {
                                            String foodName = dataSnapshot.child("foodname").getValue().toString();
                                            userFoodsViewHolder.layoutfoodname.setText(foodName);
                                        }
                                        if(dataSnapshot.hasChild("foodcalories"))
                                        {
                                            String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                            foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(dinnerNumberOfServings) * Double.parseDouble(foodCalories)) / Double.parseDouble(dinnerServingSize));
                                            userFoodsViewHolder.layoutfoodcalorie.setText(foodCalories);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            dinnerNumberOfServings = "1";
                            dinnerServingSize = "1";
                        }

                    }

                    @NonNull
                    @Override
                    public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                        UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                        return userFoodsViewHolder;
                    }
                };

        userDinnerFoodList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    private void DisplayUserAllSnacks(final String userID, final String date)
    {
        Query userDinner = foodDiaryDatabaseReference.child(userID).child(date).orderByChild("foodType").equalTo("snack");


        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(userDinner, Foods.class)
                        .build();

        FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String foodID = getRef(i).getKey();
                        if(!TextUtils.isEmpty(foodID))
                        {
                            foodDiaryDatabaseReference.child(userID).child(date).child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("numberOfServing"))
                                        {
                                            snackNumberOfServings = dataSnapshot.child("numberOfServing").getValue().toString();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            foodDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("foodname"))
                                        {
                                            String foodName = dataSnapshot.child("foodname").getValue().toString();
                                            userFoodsViewHolder.layoutfoodname.setText(foodName);
                                        }
                                        if(dataSnapshot.hasChild("foodcalories"))
                                        {
                                            String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                            foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(snackNumberOfServings) * Double.parseDouble(foodCalories)) / Double.parseDouble(snackServingSize));
                                            userFoodsViewHolder.layoutfoodcalorie.setText(foodCalories);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            snackNumberOfServings = "1";
                            snackServingSize = "1";
                        }

                    }

                    @NonNull
                    @Override
                    public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                        UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                        return userFoodsViewHolder;
                    }
                };

        userSnacksFoodList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    private void DisplayUserAllWorkouts(String userID, String date)
    {
        Query userWorkouts = workoutDiaryDatabaseReference.child(userID).child(date);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Workouts>()
                        .setQuery(userWorkouts, Workouts.class)
                        .build();

        FirebaseRecyclerAdapter<Workouts, UserFoodsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Workouts, UserFoodsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Workouts workouts)
            {
                final String workoutType = getRef(i).getKey();
                userFoodsViewHolder.layoutfoodname.setText(workoutType);
                userFoodsViewHolder.layoutfoodcalorie.setText(workouts.getCalories());
            }

            @NonNull
            @Override
            public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                return userFoodsViewHolder;
            }
        };
        userWorkoutList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }




    public static class UserFoodsViewHolder extends RecyclerView.ViewHolder
    {
        TextView layoutfoodname, layoutfoodcalorie;

        public UserFoodsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            layoutfoodname = (TextView)itemView.findViewById(R.id.userfood_name);
            layoutfoodcalorie = (TextView)itemView.findViewById(R.id.userfood_calories);
        }
    }



    private void DisplayUserWaterGlasses(String userID, String date)
    {
        warerGlassesContainer.setVisibility(View.GONE);

        waterDiaryDatabaseReference.child(userID).child(date).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("glasses"))
                    {
                        if(!TextUtils.isEmpty(dataSnapshot.child("glasses").getValue().toString()) &&
                                (Integer.parseInt(dataSnapshot.child("glasses").getValue().toString()) > 0))
                        {
                            warerGlassesContainer.setVisibility(View.VISIBLE);
                            waterGlasses.setText(dataSnapshot.child("glasses").getValue().toString());
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


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        //onBackPressed();
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "home");
        startActivity(mainIntent);
        finish();
        return true;
    }
}
