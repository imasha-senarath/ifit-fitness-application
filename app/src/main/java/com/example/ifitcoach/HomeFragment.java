package com.example.ifitcoach;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;


public class HomeFragment extends Fragment
{
    View view;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, foodDiaryDatabaseReference, foodDatabaseReference, waterDiaryDatabaseReference,
            workoutDiaryDatabaseReference, achievementDatabaseReference;

    private String currentUserID;
    private ProgressDialog loadingBar;
    private String intentFrom;
    private long diariesFoodCount, diariesWorkoutCount;
    String weightGoalStatus="", weightAchievementStatus="", points="0";


    private ProgressBar summaryCardCaloriesBar;
    private TextView summaryCardFinalCalories, summaryCardTDEE01, summaryCardTDEE02, summaryCardFoodCalories, summaryCardWorkoutCalories,
            summaryCardCaloriesRemaining, summaryCardTips;
    private int summaryCardCaloriesBarValue;
    private LinearLayout SummaryCard;


    private TextView foodCardConsumedCalories;
    private LinearLayout foodCardBreakfastBtn, foodCardLunchBtn, foodCardDinnerBtn, foodCardSnackBtn;


    private TextView workoutCardBurnedCalories;
    private LinearLayout workoutCardWalkingBtn, workoutCardRunningBtn, workoutCardCyclingBtn, workoutCardMoreBtn;


    private ImageButton waterCardWaterAddBtn, waterCardWaterMinusBtn;
    private TextView waterCardGlasses;


    private Button weightCardGoalBtn, weightCardRecordBtn;
    private TextView weightCardCurrentWeight, weightCardGoalWeight, weightCardGoalCompleteDescription;
    private ImageButton weightCardWeightMinusBtn, weightCardWeightAddBtn;
    private ProgressBar weightCardGoalProgress;
    private LinearLayout weightCardGoalContainer;


    private TextView bmiCardBmi, bmiCardTips;
    private ImageView bmiCardUnderWeightStatus, bmiCardHealthyWeightStatus, bmiCardOverWeightStatus, bmiCardObesityStatus;


    private String strUserCurrentWeight, strUserGoalWeight, strUserWeeklyGoalWeight, strUserStartWeight, strUserBYear, strUserAge, strUserHeight,
            strUserGender, strUserActivityLevel, strUserFoodCalories="0", strUserWorkout="0", strUserServingSize="1", strUserNumberOfServing="1",strUserFinalCalories,
            strUserTDEE, strUserCaloriesRemaining, strUserBmi, strUserHealthStatus, strUserWaterGlasses;


    public HomeFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);


        /* getting current date */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = simpleDateFormat.format(calendar.getTime());


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        foodDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("FoodDiaries").child(currentUserID).child(currentDate);
        waterDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WaterDiaries").child(currentUserID).child(currentDate);
        workoutDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WorkoutDiaries").child(currentUserID).child(currentDate);
        achievementDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Achievements").child(currentUserID);


        /* getting intent string */
        Intent intent = getActivity().getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        /* identifying whether first execution or not */
        if(intentFrom.equals("SplashActivity") || intentFrom.equals("LoginActivity") || intentFrom.equals("CreateAccountActivity"))
        {
            /* if it is first time, adding the loading dialog */
            loadingBar = new ProgressDialog(getContext());
            String ProgressDialogMessage="Initialising...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingBar.setMessage(spannableMessage);
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.setCancelable(false);

            intent.putExtra("intentFrom", "null");
        }


        SummaryCard = view.findViewById(R.id.home_summary_card);
        summaryCardCaloriesBar = (ProgressBar) view.findViewById(R.id.home_summary_card_calories_bar);
        summaryCardFinalCalories = (TextView) view.findViewById(R.id.home_summary_card_final_calories);
        summaryCardTDEE01 = (TextView) view.findViewById(R.id.home_summary_card_TDEE_01);
        summaryCardTDEE02 = (TextView) view.findViewById(R.id.home_summary_card_TDEE_02);
        summaryCardFoodCalories = (TextView) view.findViewById(R.id.home_summary_card_food_calories);
        summaryCardWorkoutCalories = (TextView) view.findViewById(R.id.home_summary_card_workout_calories);
        summaryCardCaloriesRemaining = (TextView) view.findViewById(R.id.home_summary_card_calories_remaining);
        summaryCardTips = (TextView)view.findViewById(R.id.home_summary_card_tips);


        foodCardConsumedCalories = (TextView) view.findViewById(R.id.home_food_card_consumed_calories);
        foodCardBreakfastBtn = (LinearLayout) view.findViewById(R.id.home_food_card_breakfast_btn);
        foodCardLunchBtn = (LinearLayout) view.findViewById(R.id.home_food_card_lunch_btn);
        foodCardDinnerBtn= (LinearLayout) view.findViewById(R.id.home_food_card_dinner_btn);
        foodCardSnackBtn= (LinearLayout) view.findViewById(R.id.home_food_card_snack_btn);


        workoutCardBurnedCalories = (TextView)view.findViewById(R.id.home_workout_card_burned_calories);
        workoutCardWalkingBtn = (LinearLayout) view.findViewById(R.id.home_workout_card_walking_button);
        workoutCardRunningBtn = (LinearLayout) view.findViewById(R.id.home_workout_card_running_button);
        workoutCardCyclingBtn = (LinearLayout) view.findViewById(R.id.home_workout_card_cycling_button);
        workoutCardMoreBtn = (LinearLayout) view.findViewById(R.id.home_workout_card_more_button);


        waterCardGlasses = (TextView) view.findViewById(R.id.home_water_card_glasses);
        waterCardWaterAddBtn = (ImageButton) view.findViewById(R.id.home_water_card_glasses_add_button);
        waterCardWaterMinusBtn = (ImageButton) view.findViewById(R.id.home_water_card_glasses_minus_button);


        weightCardGoalBtn = (Button) view.findViewById(R.id.home_weight_card_goal_button);
        weightCardRecordBtn = (Button) view.findViewById(R.id.home_weight_card_record_button);
        weightCardWeightMinusBtn = (ImageButton) view.findViewById(R.id.home_weight_card_weight_minus_button);
        weightCardWeightAddBtn = (ImageButton) view.findViewById(R.id.home_weight_card_weight_add_button);
        weightCardCurrentWeight = (TextView)view.findViewById(R.id.home_weight_card_current_weight);
        weightCardGoalWeight = (TextView) view.findViewById(R.id.home_weight_card_goal_weight);
        weightCardGoalProgress = view.findViewById(R.id.home_weight_card_weight_goal_progress);
        weightCardGoalContainer = view.findViewById(R.id.home_weight_card_goal_container);
        weightCardGoalContainer.setVisibility(View.VISIBLE);
        weightCardGoalCompleteDescription = view.findViewById(R.id.home_weight_card_goal_complete_description);
        weightCardGoalCompleteDescription.setVisibility(View.GONE);


        bmiCardBmi = (TextView) view.findViewById(R.id.home_bmi_card_bmi);
        bmiCardUnderWeightStatus = (ImageView)view.findViewById(R.id.home_bmi_card_under_weight_status);
        bmiCardUnderWeightStatus.setVisibility(View.INVISIBLE);
        bmiCardHealthyWeightStatus = (ImageView)view.findViewById(R.id.home_bmi_card_healthy_weight_status);
        bmiCardHealthyWeightStatus.setVisibility(View.INVISIBLE);
        bmiCardOverWeightStatus = (ImageView)view.findViewById(R.id.home_bmi_card_over_weight_status);
        bmiCardOverWeightStatus.setVisibility(View.INVISIBLE);
        bmiCardObesityStatus = (ImageView)view.findViewById(R.id.home_bmi_card_obesity_status);
        bmiCardObesityStatus.setVisibility(View.INVISIBLE);
        bmiCardTips = (TextView) view.findViewById(R.id.home_bmi_card_tips);


        SummaryCard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToDiaryPage();
            }
        });


        foodCardBreakfastBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFoodListPage("Breakfast");
            }
        });

        foodCardLunchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFoodListPage("Lunch");
            }
        });

        foodCardDinnerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFoodListPage("Dinner");
            }
        });

        foodCardSnackBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFoodListPage("Snack");
            }
        });


        workoutCardWalkingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Walking");
            }
        });

        workoutCardRunningBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Running");
            }
        });

        workoutCardCyclingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Cycling");
            }
        });



        workoutCardMoreBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenWorkoutMenuDialog();
            }
        });


        waterCardWaterAddBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                WaterAddCalculation();
            }
        });


        waterCardWaterMinusBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                WaterMinusCalculation();
            }
        });


        weightCardGoalBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToGoalsPage();
            }
        });

        weightCardRecordBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenWeightEditDialog();
            }
        });

        weightCardWeightMinusBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                WeightMinusCalculation();
            }
        });


        weightCardWeightAddBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                WeightAddCalculation();
            }
        });


        GetUserData();

        return view;
    }



    private void RetrieveAchievementData()
    {
        achievementDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("weightLossGoalStatus"))
                    {
                        weightGoalStatus = dataSnapshot.child("weightLossGoalStatus").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("weightLossAchievementStatus"))
                    {
                        weightAchievementStatus = dataSnapshot.child("weightLossAchievementStatus").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("points"))
                    {
                        points = dataSnapshot.child("points").getValue().toString();
                    }

                    if(weightGoalStatus.equals("true") && weightAchievementStatus.equals("false"))
                    {
                        if(!strUserCurrentWeight.isEmpty() && !strUserGoalWeight.isEmpty())
                        {
                            if(Double.parseDouble(strUserCurrentWeight) <= Double.parseDouble(strUserGoalWeight))
                            {
                                /* checking whether the fragment is visible or not */
                                if(HomeFragment.this.isVisible())
                                {
                                    PopupAchievementDialog(points);
                                }
                            }
                        }
                    }

                    if(weightGoalStatus.equals("true") && weightAchievementStatus.equals("false"))
                    {
                        weightCardGoalContainer.setVisibility(View.VISIBLE);
                        weightCardGoalCompleteDescription.setVisibility(View.GONE);
                    }
                    else
                    {
                        weightCardGoalContainer.setVisibility(View.GONE);
                        weightCardGoalCompleteDescription.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void PopupAchievementDialog(String points)
    {
        points = String.format(Locale.US,"%02d",(Integer.parseInt(points) + 5));
        HashMap achievementMap = new HashMap();
        achievementMap.put("points", points);
        achievementMap.put("weightLossAchievementStatus", "true");
        achievementDatabaseReference.updateChildren(achievementMap);


        final Dialog achievementDialog = new Dialog(getActivity());
        achievementDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        achievementDialog.setContentView(R.layout.achievements_layout);
        achievementDialog.setTitle("Achievement Window");
        achievementDialog.show();
        achievementDialog.setCanceledOnTouchOutside(false);
        achievementDialog.setCancelable(false);
        Window window = achievementDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView dialogDescription = achievementDialog.findViewById(R.id.achievement_dialog_description);
        dialogDescription.setText("Congratulations, You have achieved your weight loss goal.");

        TextView dialogPoints = achievementDialog.findViewById(R.id.achievement_dialog_points);
        dialogPoints.setText("+5 Points");

        ImageView cancelBtn = achievementDialog.findViewById(R.id.achievement_dialog_close_button);
        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                achievementDialog.dismiss();
            }
        });
    }


    private void OpenWeightEditDialog()
    {
        final Dialog weightdialog = new Dialog(getContext());
        weightdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        weightdialog.setContentView(R.layout.weight_edit_layout);
        weightdialog.setTitle("weight edit window");
        weightdialog.show();
        Window weightWindow = weightdialog.getWindow();
        weightWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText editCurrentWeight = (EditText) weightdialog.findViewById(R.id.weight_edit_dialog_inputcurrentweight);
        final TextView errormsg = (TextView) weightdialog.findViewById(R.id.weight_edit_dialog_error_msg);
        errormsg.setVisibility(View.GONE);


        if(!TextUtils.isEmpty(strUserCurrentWeight))
        {
            editCurrentWeight.setText(strUserCurrentWeight);
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
                    HashMap userMap = new HashMap();
                    userMap.put("usercurrentweight", editCurrentWeight.getText().toString());
                    userDatabaseReference.updateChildren(userMap);
                    weightdialog.cancel();
                }
            }
        });
    }


    private void WeightMinusCalculation()
    {
        if(!TextUtils.isEmpty(strUserCurrentWeight) && (Double.parseDouble(strUserCurrentWeight)) > 10)
        {
            NumberFormat nf = DecimalFormat.getInstance();
            nf.setMaximumFractionDigits(1);

            strUserCurrentWeight = nf.format(Double.parseDouble(strUserCurrentWeight) - 0.1);
            strUserCurrentWeight = strUserCurrentWeight.replace(",","");

            HashMap userMap = new HashMap();
            userMap.put("usercurrentweight", strUserCurrentWeight);
            userDatabaseReference.updateChildren(userMap);
        }

        if(TextUtils.isEmpty(strUserCurrentWeight))
        {
            OpenWeightEditDialog();
        }

        if((Double.parseDouble(strUserCurrentWeight)) <= 10)
        {
            Toast.makeText(getActivity(), "Invalid Weight", Toast.LENGTH_SHORT).show();
        }
    }



    private void WeightAddCalculation()
    {
        if(!TextUtils.isEmpty(strUserCurrentWeight))
        {
            NumberFormat nf = DecimalFormat.getInstance();
            nf.setMaximumFractionDigits(1);

            strUserCurrentWeight = nf.format(Double.parseDouble(strUserCurrentWeight) + 0.1);
            strUserCurrentWeight = strUserCurrentWeight.replace(",","");

            HashMap userMap = new HashMap();
            userMap.put("usercurrentweight", strUserCurrentWeight);
            userDatabaseReference.updateChildren(userMap);
        }
        else
        {
            OpenWeightEditDialog();
        }
    }


    private void WaterMinusCalculation()
    {
        if(!TextUtils.isEmpty(strUserWaterGlasses) && (Integer.parseInt(strUserWaterGlasses)) > 0)
        {
            strUserWaterGlasses = String.format("%02d",(Integer.parseInt(strUserWaterGlasses) - 1));
            strUserWaterGlasses = strUserWaterGlasses.replace(",","");

            HashMap waterDiaryMap = new HashMap();
            waterDiaryMap.put("glasses", strUserWaterGlasses);
            waterDiaryDatabaseReference.updateChildren(waterDiaryMap);
        }
    }


    private void WaterAddCalculation()
    {
        if(!TextUtils.isEmpty(strUserWaterGlasses))
        {
            strUserWaterGlasses = String.format("%02d",(Integer.parseInt(strUserWaterGlasses) + 1));
            strUserWaterGlasses = strUserWaterGlasses.replace(",","");

            HashMap waterDiaryMap = new HashMap();
            waterDiaryMap.put("glasses", strUserWaterGlasses);
            waterDiaryDatabaseReference.updateChildren(waterDiaryMap);
        }
        else
        {
            HashMap waterDiaryMap = new HashMap();
            waterDiaryMap.put("glasses", "01");
            waterDiaryDatabaseReference.updateChildren(waterDiaryMap);
        }
    }


    private void OpenWorkoutMenuDialog()
    {
        final Dialog workoutMenu = new Dialog(getContext());
        workoutMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        workoutMenu.setContentView(R.layout.workout_menu_layout);
        workoutMenu.setTitle("Workout Window");
        workoutMenu.show();
        Window setgoalWindow = workoutMenu.getWindow();
        setgoalWindow.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout walkingBtn = (RelativeLayout) workoutMenu.findViewById(R.id.workout_menu_walking_button);
        walkingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Walking");
                workoutMenu.dismiss();
            }
        });

        RelativeLayout runningBtn = (RelativeLayout) workoutMenu.findViewById(R.id.workout_menu_running_button);
        runningBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Running");
                workoutMenu.dismiss();
            }
        });

        RelativeLayout cyclingBtn = (RelativeLayout) workoutMenu.findViewById(R.id.workout_menu_cycling_button);
        cyclingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Cycling");
                workoutMenu.dismiss();
            }
        });

        RelativeLayout hikingBtn = (RelativeLayout) workoutMenu.findViewById(R.id.workout_menu_hiking_button);
        hikingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Hiking");
                workoutMenu.dismiss();
            }
        });

        RelativeLayout swimmingBtn = (RelativeLayout) workoutMenu.findViewById(R.id.workout_menu_swimming_button);
        swimmingBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToAddWorkoutPage("Swimming");
                workoutMenu.dismiss();
            }
        });
    }


    public void GetUserData()
    {
        /* getting user common details */
        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("usercurrentweight"))
                    {
                        strUserCurrentWeight = dataSnapshot.child("usercurrentweight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userstartweight"))
                    {
                        strUserStartWeight = dataSnapshot.child("userstartweight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("usergoalweight"))
                    {
                        strUserGoalWeight = dataSnapshot.child("usergoalweight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userheight"))
                    {
                        strUserHeight = dataSnapshot.child("userheight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userbyear"))
                    {
                        strUserBYear = dataSnapshot.child("userbyear").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("usergender"))
                    {
                        strUserGender = dataSnapshot.child("usergender").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("useractivitylevel"))
                    {
                        strUserActivityLevel = dataSnapshot.child("useractivitylevel").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userweeklygoalweight"))
                    {
                        strUserWeeklyGoalWeight = dataSnapshot.child("userweeklygoalweight").getValue().toString();
                    }

                    GetUserFoodData();
                    RetrieveAchievementData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        /* getting water details */
        waterDiaryDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("glasses"))
                    {
                        strUserWaterGlasses = dataSnapshot.child("glasses").getValue().toString();
                        if(!TextUtils.isEmpty(strUserWaterGlasses))
                        {
                            waterCardGlasses.setText(strUserWaterGlasses);
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



    /* getting user food details from firebase database  */
    private void GetUserFoodData()
    {
        foodDiaryDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    /* count the user foods */
                    diariesFoodCount = dataSnapshot.getChildrenCount();
                    for(final DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        /* getting the food ID */
                        String foodID = ds.getKey();

                        foodDiaryDatabaseReference.child(foodID).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("numberOfServing"))
                                    {
                                        strUserNumberOfServing = dataSnapshot.child("numberOfServing").getValue().toString();
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
                                        strUserServingSize = dataSnapshot.child("foodservingsize").getValue().toString();
                                    }

                                    if(dataSnapshot.hasChild("foodcalories"))
                                    {
                                        String foodCalories = dataSnapshot.child("foodcalories").getValue().toString();
                                        foodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(strUserNumberOfServing) * Double.parseDouble(foodCalories)) / Double.parseDouble(strUserServingSize));
                                        strUserFoodCalories = String.format(Locale.US,"%.0f",(Double.parseDouble(strUserFoodCalories) + Double.parseDouble(foodCalories)));
                                    }

                                    strUserServingSize="1";
                                    strUserNumberOfServing="1";

                                    diariesFoodCount = diariesFoodCount - 1;

                                    /* checking whether  finished the calculation or not */
                                    if(diariesFoodCount == 0)
                                    {
                                        GetUserWorkoutData();
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
                    GetUserWorkoutData();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




    /* getting user workout details from firebase database */
    private void GetUserWorkoutData()
    {
        workoutDiaryDatabaseReference.addValueEventListener(new ValueEventListener()
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

                        workoutDiaryDatabaseReference.child(workoutType).addValueEventListener(new ValueEventListener()
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
                                    SetUserDataToHomePage();
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
                    SetUserDataToHomePage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    /* setting user details */
    private void SetUserDataToHomePage()
    {
        FormulaCalculations fc = new FormulaCalculations();

        if(!TextUtils.isEmpty(strUserCurrentWeight) && !TextUtils.isEmpty( strUserBYear) &&
                (!TextUtils.isEmpty( strUserHeight))
                && !TextUtils.isEmpty(strUserActivityLevel) && !TextUtils.isEmpty( strUserGender))
        {


            strUserAge = fc.Age(strUserBYear);

            strUserTDEE = fc.TDEE(strUserAge, strUserCurrentWeight, strUserHeight, strUserGender, strUserActivityLevel);


            weightCardCurrentWeight.setText(strUserCurrentWeight);


            /* ###### Start of the BMI Card Calculations ###### */
            strUserBmi = fc.BMI(strUserCurrentWeight, strUserHeight);
            bmiCardBmi.setText(strUserBmi);


            strUserHealthStatus = fc.HealthStatus(strUserBmi);


            /* set health status*/
            if (strUserHealthStatus.equals("Underweight")) {
                bmiCardUnderWeightStatus.setVisibility(View.VISIBLE);
                bmiCardHealthyWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardOverWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardObesityStatus.setVisibility(View.INVISIBLE);
                bmiCardTips.setText("You fall within the Under Weight range. Try to increase your weight.");
            }
            if (strUserHealthStatus.equals("HealthyWeight")) {
                bmiCardUnderWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardHealthyWeightStatus.setVisibility(View.VISIBLE);
                bmiCardOverWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardObesityStatus.setVisibility(View.INVISIBLE);
                bmiCardTips.setText("You fall within the Healthy Weight range. Try to maintain your weight.");
            }
            if (strUserHealthStatus.equals("Overweight")) {
                bmiCardUnderWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardHealthyWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardOverWeightStatus.setVisibility(View.VISIBLE);
                bmiCardObesityStatus.setVisibility(View.INVISIBLE);
                bmiCardTips.setText("You fall within the Over Weight range. Try to decrease your weight.");
            }
            if (strUserHealthStatus.equals("Obesity")) {
                bmiCardUnderWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardHealthyWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardOverWeightStatus.setVisibility(View.INVISIBLE);
                bmiCardObesityStatus.setVisibility(View.VISIBLE);
                bmiCardTips.setText("You fall within the Over Weight range. Try to decrease your weight.");
            }
            /* ###### end of the BMI Card Calculations ###### */
        }
        else
        {
            summaryCardTips.setText("Please setup your profile for more accuracy.");
            /* set default TDEE */
            strUserTDEE = "2300";
            bmiCardTips.setText("Please setup your profile for check your Body Mass Index.");
        }

        /* Set weight card details  */
        if(!TextUtils.isEmpty(strUserWeeklyGoalWeight) && !TextUtils.isEmpty(strUserGoalWeight) && weightGoalStatus.equals("true") &&
                weightAchievementStatus.equals("false"))
        {
            weightCardGoalWeight.setText(strUserGoalWeight);

            String cutCaloriesPerDay = String.format(Locale.US,"%.0f",(((Double.parseDouble(strUserWeeklyGoalWeight) * 3500) / 0.45)) / 7);
            strUserTDEE = String.valueOf(Integer.parseInt(strUserTDEE) - Integer.parseInt(cutCaloriesPerDay));

            String lossWeightGoal = String.format(Locale.US,"%.1f",(Double.parseDouble(strUserStartWeight) - Double.parseDouble(strUserGoalWeight)));
            String lostWeight = String.format(Locale.US,"%.1f",(Double.parseDouble(strUserStartWeight) - Double.parseDouble(strUserCurrentWeight)));

            String weightLossProgressValue = String.format(Locale.US,"%.0f",((Double.parseDouble(lostWeight) * 100) / Double.parseDouble(lossWeightGoal)));
            weightCardGoalProgress.setProgress(Integer.parseInt(weightLossProgressValue));
        }
        else
        {
            weightCardGoalContainer.setVisibility(View.GONE);
            weightCardGoalCompleteDescription.setVisibility(View.VISIBLE);
        }

        /* for new the user */
        if(TextUtils.isEmpty(strUserGoalWeight))
        {
            weightCardGoalCompleteDescription.setText("Decrease your weight by Setting a goal and earn points.");
        }


        summaryCardFoodCalories.setText(strUserFoodCalories);
        summaryCardWorkoutCalories.setText(strUserWorkout);
        foodCardConsumedCalories.setText(strUserFoodCalories);
        workoutCardBurnedCalories.setText(strUserWorkout);


        strUserFinalCalories = String.valueOf(Integer.parseInt(strUserFoodCalories) - Integer.parseInt(strUserWorkout));
        summaryCardFinalCalories.setText(strUserFinalCalories);


        summaryCardTDEE01.setText("/ "+strUserTDEE);
        summaryCardTDEE02.setText(strUserTDEE);

        strUserCaloriesRemaining = fc.CaloriesRemaining(strUserTDEE, strUserFoodCalories, strUserWorkout);
        summaryCardCaloriesRemaining.setText(strUserCaloriesRemaining);


        summaryCardCaloriesBarValue = fc.ProgressBarValue(strUserTDEE, strUserCaloriesRemaining);
        summaryCardCaloriesBar.setProgress(summaryCardCaloriesBarValue);


        if(Integer.parseInt(strUserCaloriesRemaining) < 0)
        {
            summaryCardTips.setTextColor(getResources().getColor(R.color.WarningTextColor));
            summaryCardTips.setText("You have eaten too much food.");
            summaryCardCaloriesRemaining.setTextColor(getResources().getColor(R.color.WarningTextColor));
        }



        strUserFoodCalories = "0";
        strUserWorkout = "0";

        if(intentFrom.equals("SplashActivity") || intentFrom.equals("LoginActivity") || intentFrom.equals("CreateAccountActivity"))
        {
            loadingBar.dismiss();
        }

    }


    /* User redirect to nutrition page */
    private void UserSendToFoodListPage(String intentFrom)
    {
        Intent foodListIntent = new Intent(getActivity(), FoodListActivity.class);
        foodListIntent.putExtra("intentFrom", intentFrom);
        startActivity(foodListIntent);
    }



    private void UserSendToDiaryPage()
    {
        Intent diaryIntent = new Intent(getActivity(), DiaryActivity.class);
        diaryIntent.putExtra("intentFrom", "Home");
        diaryIntent.putExtra("intentUserID", currentUserID);
        startActivity(diaryIntent);
    }

    private void UserSendToAddWorkoutPage(String intentFrom)
    {
        Intent addWorkoutIntent = new Intent(getActivity(), AddWorkoutActivity.class);
        addWorkoutIntent.putExtra("intentFrom", intentFrom);
        startActivity(addWorkoutIntent);
    }

    private void UserSendToGoalsPage()
    {
        Intent goalIntent = new Intent(getActivity(), GoalsActivity.class);
        startActivity(goalIntent);
    }
}