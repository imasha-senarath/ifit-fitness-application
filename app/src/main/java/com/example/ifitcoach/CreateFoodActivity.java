package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateFoodActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference, foodDatabaseReference;

    private EditText foodName, servingSize, servingSizeUnit, calories, carbs, fat, protein;
    private TextView warning;
    private Button createFoodbtn;

    private ProgressDialog loadingbar;

    private String currentUserID;
    String intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_food);


        /* Adding tool bar & title to nutrition activity and hide user image and notification icon */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Food");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");

        warning = (TextView) findViewById(R.id.create_food_warning);
        warning.setVisibility(View.INVISIBLE);

        foodName = (EditText) findViewById(R.id.create_food_name);
        servingSize = (EditText) findViewById(R.id.create_food_servingSize);
        servingSizeUnit = (EditText) findViewById(R.id.create_food_servingSizeUnit);
        calories = (EditText) findViewById(R.id.create_food_calories);
        carbs = (EditText) findViewById(R.id.create_food_carbs);
        fat = (EditText) findViewById(R.id.create_food_fat);
        protein = (EditText) findViewById(R.id.create_food_protien);
        createFoodbtn = (Button) findViewById(R.id.create_food_button);

        loadingbar = new ProgressDialog(this);

        createFoodbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CreateNewFood();
            }
        });
    }



    private void CreateNewFood()
    {
        String foodNameString = foodName.getText().toString();
        String servingSizeString = servingSize.getText().toString();
        String servingSizeUnitString = servingSizeUnit.getText().toString();
        String caloriesString = calories.getText().toString();
        String carbsString = carbs.getText().toString();
        String fatString = fat.getText().toString();
        String proteinString = protein.getText().toString();

        if(TextUtils.isEmpty(foodNameString)  ||   TextUtils.isEmpty(servingSizeString) || TextUtils.isEmpty(servingSizeUnitString) || TextUtils.isEmpty(caloriesString) ||
                TextUtils.isEmpty(carbsString) || TextUtils.isEmpty(fatString) || TextUtils.isEmpty(proteinString))
        {
            warning.setVisibility(View.VISIBLE);
        }
        else
        {
            warning.setVisibility(View.INVISIBLE);

            /* adding Loading bar */
            loadingbar = new ProgressDialog(this);
            String ProgressDialogMessage="Creating Food...";
            SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
            spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
            loadingbar.setMessage(spannableMessage);
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.setCancelable(false);


            HashMap usermap = new HashMap();
            usermap.put("foodname", foodNameString);
            usermap.put("foodsearchkeyword", foodNameString.toLowerCase());
            usermap.put("foodservingsize", servingSizeString);
            usermap.put("foodservingsizeunit", servingSizeUnitString);
            usermap.put("foodcalories", caloriesString);
            usermap.put("foodcarbs", carbsString);
            usermap.put("foodfat", fatString);
            usermap.put("foodprotein", proteinString);
            usermap.put("foodcreator", currentUserID);


            /* get current date */
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat currenDate = new SimpleDateFormat("yyyyMMdd");
            String CurrentDate = currenDate.format(calendar1.getTime());

            /* get current time */
            Calendar calendar2 = Calendar.getInstance();
            SimpleDateFormat currenTime = new SimpleDateFormat("HHmmss");
            String CurrentTime = currenTime.format(calendar2.getTime());

            String foodRandomName = foodNameString + currentUserID + CurrentDate + CurrentTime;
            foodRandomName = foodRandomName.replace(" ","");
            foodRandomName = foodRandomName.toLowerCase();

            foodDatabaseReference.child(foodRandomName).updateChildren(usermap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        loadingbar.dismiss();
                        Toast.makeText(CreateFoodActivity.this, "Food Create Successfully.", Toast.LENGTH_SHORT).show();
                        UserSendToFoodListPage();
                    }
                    else
                    {
                        loadingbar.dismiss();
                        String errorMsg = task.getException().getMessage();
                        warning.setText(errorMsg);
                    }
                }
            });
        }
    }

    private void UserSendToFoodListPage()
    {
        Intent foodListIntent = new Intent(CreateFoodActivity.this, FoodListActivity.class);
        foodListIntent.putExtra("intentFrom", intentFrom);
        startActivity(foodListIntent);
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
