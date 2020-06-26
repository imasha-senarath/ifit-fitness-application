package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFoodActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference diaryDatabaseReference, foodDatabaseReference, userDatabaseReference;

    private TextView foodName, servingSize, numberOfServings, calories, carbs, fat, protein, createdBy;
    private Button addbtn;
    private LinearLayout numberOfServingsContainer;

    private String foodNameValue, servingSizeValue, numberOfSizeValue="1", servingSizeUnitValue, caloriesValue, carbsValue, fatValue, proteinValue,
            foodCreator, createdByValue;


    String intentFrom, intentFoodID;

    String currentUserID;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);


        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Food");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* hidden toolbar user image */
        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        diaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("FoodDiaries");
        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("Breakfast") || intentFrom.equals("Lunch") ||
                intentFrom.equals("Dinner") ||  intentFrom.equals("Snack") || intentFrom.equals("ProfileMenu"))
        {
            intentFoodID = intent.getExtras().getString("intentFoodID");
        }


        foodName = (TextView) findViewById(R.id.addfood_foodname);
        servingSize = (TextView) findViewById(R.id.add_food_serving_size);
        numberOfServings = (TextView) findViewById(R.id.add_food_number_of_serving);
        calories = (TextView) findViewById(R.id.addfood_calories);
        carbs = (TextView) findViewById(R.id.addfood_carbs);
        fat = (TextView) findViewById(R.id.addfood_fat);
        protein = (TextView) findViewById(R.id.addfood_protein);
        createdBy = (TextView) findViewById(R.id.addfood_createdby);
        numberOfServingsContainer = findViewById(R.id.add_food_number_of_servings_container);


        addbtn = (Button) findViewById(R.id.addfood_addbutton);
        if(!intentFrom.equals("ProfileMenu"))
        {
            addbtn.setText("Add to"+" "+intentFrom);
        }


        loadingbar = new ProgressDialog(this);


        if(!TextUtils.isEmpty(intentFoodID))
        {
            GetAndSetFoodDetails();
        }


        addbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddFoodToUserDiary();
            }
        });

        numberOfServingsContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupServingSizeEditDialog();
            }
        });
    }



    private void PopupServingSizeEditDialog()
    {
        final Dialog numberOfServingEditDialog = new Dialog(this);
        numberOfServingEditDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        numberOfServingEditDialog.setContentView(R.layout.number_of_servings_edit_layout);
        numberOfServingEditDialog.setTitle("Number Of Serving Edit Window");
        numberOfServingEditDialog.show();
        Window servingSizeEditWindow = numberOfServingEditDialog.getWindow();
        servingSizeEditWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText numberOfServingsInput = (EditText) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_input);
        final TextView errorMsg = (TextView) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_error);
        errorMsg.setVisibility(View.GONE);


        /* cancel button click action */
        Button cancelBtn = (Button) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                numberOfServingEditDialog.cancel();
            }
        });


        /* submit button click action */
        Button submitBtn = (Button)numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_submit_button);
        submitBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.isEmpty(numberOfServingsInput.getText().toString()) || Double.parseDouble(numberOfServingsInput.getText().toString()) <= 0)
                {
                    errorMsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    numberOfSizeValue = numberOfServingsInput.getText().toString();
                    numberOfServingEditDialog.cancel();
                    NewValueCalculation();
                }
            }
        });
    }



    private void NewValueCalculation()
    {
        numberOfServings.setText(numberOfSizeValue);

        String newCaloriesValue = String.format(Locale.US,"%.0f",(Double.parseDouble(numberOfSizeValue) * Double.parseDouble(caloriesValue)) / Double.parseDouble(servingSizeValue));
        calories.setText(newCaloriesValue +" Calories");

        String newCrabsValue = String.format(Locale.US,"%.1f",(Double.parseDouble(numberOfSizeValue) * Double.parseDouble(carbsValue)) / Double.parseDouble(servingSizeValue));
        carbs.setText(newCrabsValue+"g");

        String newFatValue = String.format(Locale.US,"%.1f",(Double.parseDouble(numberOfSizeValue) * Double.parseDouble(fatValue)) / Double.parseDouble(servingSizeValue));
        fat.setText(newFatValue+"g");

        String newProteinValue = String.format(Locale.US,"%.1f",(Double.parseDouble(numberOfSizeValue) * Double.parseDouble(proteinValue)) / Double.parseDouble(servingSizeValue));
        protein.setText(newProteinValue+"g");

    }


    private void GetAndSetFoodDetails()
    {
        foodDatabaseReference.child(intentFoodID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("foodname"))
                    {
                        foodNameValue = dataSnapshot.child("foodname").getValue().toString();
                        foodName.setText(foodNameValue);
                    }

                    if(dataSnapshot.hasChild("foodservingsize") && dataSnapshot.hasChild("foodservingsizeunit"))
                    {
                        servingSizeValue = dataSnapshot.child("foodservingsize").getValue().toString();
                        servingSizeUnitValue = dataSnapshot.child("foodservingsizeunit").getValue().toString();
                        servingSize.setText(servingSizeValue+" "+servingSizeUnitValue);
                    }

                    if(dataSnapshot.hasChild("foodcalories"))
                    {
                        caloriesValue = dataSnapshot.child("foodcalories").getValue().toString();
                        calories.setText(caloriesValue +" Calories");
                    }

                    if(dataSnapshot.hasChild("foodcarbs"))
                    {
                        carbsValue = dataSnapshot.child("foodcarbs").getValue().toString();
                        carbs.setText(carbsValue+"g");
                    }

                    if(dataSnapshot.hasChild("foodfat"))
                    {
                        fatValue = dataSnapshot.child("foodfat").getValue().toString();
                        fat.setText(fatValue+"g");
                    }

                    if(dataSnapshot.hasChild("foodprotein"))
                    {
                        proteinValue = dataSnapshot.child("foodprotein").getValue().toString();
                        protein.setText(proteinValue+"g");
                    }

                    if(dataSnapshot.hasChild("foodcreator"))
                    {
                        foodCreator = dataSnapshot.child("foodcreator").getValue().toString();
                        userDatabaseReference.child(foodCreator).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("username"))
                                    {
                                        createdByValue = dataSnapshot.child("username").getValue().toString();
                                        createdBy.setText(createdByValue);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });
                    }
                    else
                    {
                        createdBy.setText("iFit Team");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void AddFoodToUserDiary()
    {
        if(!intentFrom.equals("ProfileMenu"))
        {
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Adding Food...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);

            HashMap diaryMap = new HashMap();
            diaryMap.put("foodType", intentFrom.toLowerCase());
            diaryMap.put("numberOfServing", numberOfSizeValue);


            /* get current date */
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currenDate = new SimpleDateFormat("dd-MMM-yyyy");
            String currentDate = currenDate.format(calendar.getTime());



            diaryDatabaseReference.child(currentUserID).child(currentDate).child(intentFoodID).updateChildren(diaryMap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        UserSendToDiaryPage();

                    }
                    else
                    {
                        loadingbar.dismiss();
                        String msg = task.getException().getMessage();
                        Toast.makeText(AddFoodActivity.this, "Error"+msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            OpenFoodSelectDialog();
        }
    }


    private void OpenFoodSelectDialog()
    {
        final Dialog selectfooddialog = new Dialog(this);
        selectfooddialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectfooddialog.setContentView(R.layout.foodtype_select_menu_layout);
        selectfooddialog.setTitle("Select Food Window");
        selectfooddialog.show();

        RelativeLayout breakfastBtn = (RelativeLayout) selectfooddialog.findViewById(R.id.food_type_select_menu_breakfast);
        breakfastBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectfooddialog.dismiss();
                intentFrom = "Breakfast";
                AddFoodToUserDiary();
            }
        });

        RelativeLayout lunchBtn = (RelativeLayout) selectfooddialog.findViewById(R.id.food_type_select_menu_lunch);
        lunchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectfooddialog.dismiss();
                intentFrom = "Lunch";
                AddFoodToUserDiary();
            }
        });

        RelativeLayout dinnerBtn = (RelativeLayout) selectfooddialog.findViewById(R.id.food_type_select_menu_dinner);
        dinnerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectfooddialog.dismiss();
                intentFrom = "Dinner";
                AddFoodToUserDiary();
            }
        });

        RelativeLayout snackBtn = (RelativeLayout) selectfooddialog.findViewById(R.id.food_type_select_menu_snacks);
        snackBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectfooddialog.dismiss();
                intentFrom = "Snack";
                AddFoodToUserDiary();
            }
        });
    }


    private void UserSendToDiaryPage()
    {
        Intent diaryIntent = new Intent(AddFoodActivity.this, DiaryActivity.class);
        diaryIntent.putExtra("intentFrom", "AddFoodActivity");
        diaryIntent.putExtra("intentUserID", currentUserID);
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
