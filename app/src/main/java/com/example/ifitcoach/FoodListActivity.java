package com.example.ifitcoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class FoodListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference foodDatabaseReference, userDatabaseReference;

    private EditText searchBar;

    private RecyclerView foodList;

    private FloatingActionButton addfoodbtn;

    String intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


        /* Adding tool bar & title to nutrition activity and hide user image and notification icon */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Foods");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(!intentFrom.equals("ProfileMenu"))
        {
            getSupportActionBar().setTitle("Select" +" "+intentFrom);
        }


        foodList = (RecyclerView)findViewById(R.id.food_list);
        foodList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        /* define post order*/
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        foodList.setLayoutManager(linearLayoutManager);



        searchBar = (EditText)findViewById(R.id.nutrition_search_bar);
        String searchBarInput = searchBar.getText().toString().toLowerCase();
        DisplayAllFoodsNutrition(searchBarInput);

        /* search bar actions */
        searchBar.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String searchBarInput = searchBar.getText().toString().toLowerCase();
                DisplayAllFoodsNutrition(searchBarInput);
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        /* add floating button click action */
        addfoodbtn = (FloatingActionButton)findViewById(R.id.nutrition_add_new_food_button);
        addfoodbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserSendToCreateFoodPage();
            }
        });
    }




    private void DisplayAllFoodsNutrition(String searchBarInput)
    {
        Query searchFoodQuery = foodDatabaseReference.orderByChild("foodsearchkeyword").startAt(searchBarInput).endAt(searchBarInput + "\uf8ff");

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(searchFoodQuery, Foods.class)
                        .build();

        FirebaseRecyclerAdapter<Foods, FoodNutritionViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Foods, FoodNutritionViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final FoodNutritionViewHolder foodNutritionViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String FoodID = getRef(i).getKey();

                        String tempFoodName = foodNutrition.getFoodname();
                        if(!(TextUtils.isEmpty(tempFoodName)))
                        {
                            foodNutritionViewHolder.layoutfoodname.setText(tempFoodName);
                        }


                        String tempCalories = foodNutrition.getFoodcalories();
                        if(!(TextUtils.isEmpty(tempCalories)))
                        {
                            foodNutritionViewHolder.layoutcalories.setText(tempCalories+"cal");
                        }


                        foodNutritionViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                SendUserToAddFoodPage(FoodID);
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public FoodNutritionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_layout, parent, false);
                        FoodNutritionViewHolder viewHolder = new FoodNutritionViewHolder(view);
                        return viewHolder;
                    }
                };

        foodList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void SendUserToAddFoodPage(String foodID)
    {
        if(!intentFrom.equals("ProfileMenu"))
        {
            Intent addFoodIntent = new Intent(FoodListActivity.this, AddFoodActivity.class);
            addFoodIntent.putExtra("intentFrom", intentFrom);
            addFoodIntent.putExtra("intentFoodID", foodID);
            startActivity(addFoodIntent);
        }
        else
        {
            Intent addFoodIntent = new Intent(FoodListActivity.this, AddFoodActivity.class);
            addFoodIntent.putExtra("intentFrom", intentFrom);
            addFoodIntent.putExtra("intentFoodID", foodID);
            startActivity(addFoodIntent);
        }
    }


    public static class FoodNutritionViewHolder extends RecyclerView.ViewHolder
    {
        TextView layoutfoodname, layoutcalories;


        public FoodNutritionViewHolder(@NonNull View itemView)
        {
            super(itemView);

            layoutfoodname = (TextView)itemView.findViewById(R.id.food_name);
            layoutcalories = (TextView)itemView.findViewById(R.id.food_calories);
        }
    }


    private void UserSendToCreateFoodPage()
    {
        Intent createFoodIntent = new Intent(FoodListActivity.this, CreateFoodActivity.class);
        createFoodIntent.putExtra("intentFrom", intentFrom);
        startActivity(createFoodIntent);
    }



    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
