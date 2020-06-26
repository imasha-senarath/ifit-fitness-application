package com.example.ifitcoach;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment
{
    private View view;


    private RelativeLayout postsBtn, diaryBtn, servicesBtn, servicesManagerBtn, orderBtn, nutritionBtn, logoutBtn, messagesBtn,
            requestBtn, connectionBtn, findBtn;
    private LinearLayout profileBtnContainer;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference, connectionsDatabaseReference, cancelConnectionsDatabaseReference,
            achievementDatabaseReference;

    String currentUserID;

    String userImage, username, position, accountType, points, joinedDate, currentWeight, height, activityLevel, gender, birthday, email;

    private CircleImageView profileUserImage;
    private TextView profileUsername, profilePosition, profileBadgeLevel, profilePoints, profileJoinedDate, profileWeight, profileHeight,
            profileActivityLevel, profileGender, profileBirthday, profileEmail;
    private ImageView profileBadge;

    private String connectionStatus="null";

    private Button profileReqConnectionBtn, profileSendMsgBtn;

    private ProgressDialog loadingbar;

    String intentPurpose, intentUserID;

    public ProfileFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);


        Intent intent = getActivity().getIntent();
        intentPurpose = intent.getExtras().getString("intentFrom");
        if(intentPurpose.equals("ViewAnotherUserProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        mAuth= FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        connectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections");
        achievementDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Achievements").child(currentUserID);


        profileUserImage = (CircleImageView) view.findViewById(R.id.profile_user_image);
        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profilePosition = (TextView) view.findViewById(R.id.profile_position);
        profilePosition.setVisibility(View.GONE);
        profilePoints = (TextView) view.findViewById(R.id.profile_points);
        profileBadge = view.findViewById(R.id.profile_badge);
        profileBadgeLevel = view.findViewById(R.id.profile_badge_level);
        profileJoinedDate = view.findViewById(R.id.profile_joined_date);
        profileWeight = (TextView) view.findViewById(R.id.profile_weight);
        profileHeight = (TextView) view.findViewById(R.id.profile_height);
        profileActivityLevel = (TextView) view.findViewById(R.id.profile_activity_level);
        profileBirthday = (TextView) view.findViewById(R.id.profile_birthday);
        profileGender = (TextView) view.findViewById(R.id.profile_gender);
        profileEmail = (TextView) view.findViewById(R.id.profile_email);

        profileBtnContainer = (LinearLayout) view.findViewById(R.id.profile_button_container);

        profileReqConnectionBtn = (Button) view.findViewById(R.id.profile_req_connection_button);
        profileReqConnectionBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ConnectButtonActions();
            }
        });

        profileSendMsgBtn = (Button) view.findViewById(R.id.profile_send_messages_button);
        profileSendMsgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToChatPage();
            }
        });


        postsBtn = (RelativeLayout) view.findViewById(R.id.profile_post_button);
        postsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToPostPage();
            }
        });

        servicesBtn = (RelativeLayout) view.findViewById(R.id.profile_services_button);
        servicesBtn.setVisibility(View.GONE);
        servicesBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToServicesPage();
            }
        });

        servicesManagerBtn = (RelativeLayout) view.findViewById(R.id.profile_services_manager_button);
        servicesManagerBtn.setVisibility(View.GONE);
        servicesManagerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToServicesManagerPage();
            }
        });

        logoutBtn = (RelativeLayout) view.findViewById(R.id.profile_logout_button);
        logoutBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenLogoutConfirmDialog();
            }
        });

        orderBtn = (RelativeLayout) view.findViewById(R.id.profile_orders_button);
        orderBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToOrdersPage();
            }
        });

        nutritionBtn = (RelativeLayout) view.findViewById(R.id.profile_nutrition_button);
        nutritionBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFoodListPage();
            }
        });

        diaryBtn = (RelativeLayout) view.findViewById(R.id.profile_diary_button);
        diaryBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToDiaryPage();
            }
        });

        messagesBtn = (RelativeLayout) view.findViewById(R.id.profile_messages_button);
        messagesBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToMessagesPage();
            }
        });

        requestBtn = (RelativeLayout) view.findViewById(R.id.profile_request_button);
        requestBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToRequestPage();
            }
        });

        connectionBtn = (RelativeLayout) view.findViewById(R.id.profile_connections_button);
        connectionBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToConnectionsPage();
            }
        });

        findBtn = (RelativeLayout) view.findViewById(R.id.profile_find_button);
        findBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserSendToFindPage();
            }
        });



        if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(intentUserID);
            achievementDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Achievements").child(intentUserID);

            nutritionBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.GONE);
            messagesBtn.setVisibility(View.GONE);
            requestBtn.setVisibility(View.GONE);
            orderBtn.setVisibility(View.GONE);
            findBtn.setVisibility(View.GONE);
            servicesManagerBtn.setVisibility(View.GONE);
            RetrieveConnectionDetails();
        }
        else
        {
            profileBtnContainer.setVisibility(View.GONE);
        }


        RetrieveUserDetails();


        return view;
    }



    private void RetrieveConnectionDetails()
    {
        connectionsDatabaseReference.child(currentUserID).child(intentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("connectionStatus"))
                    {
                        connectionStatus = dataSnapshot.child("connectionStatus").getValue().toString();
                        if(!TextUtils.isEmpty(connectionStatus))
                        {
                            profileReqConnectionBtn.setText(connectionStatus);
                            if(!connectionStatus.equals("connected"))
                            {
                                diaryBtn.setEnabled(false);
                                diaryBtn.setAlpha(0.5f);

                                connectionBtn.setEnabled(false);
                                connectionBtn.setAlpha(0.5f);
                            }
                        }
                    }
                }
                else
                {
                    if(!connectionStatus.equals("connected"))
                    {
                        diaryBtn.setEnabled(false);
                        diaryBtn.setAlpha(0.5f);

                        connectionBtn.setEnabled(false);
                        connectionBtn.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void ConnectButtonActions()
    {
        if(connectionStatus.equals("requested"))
        {
            OpenRemoveRequestedConnectionConfirmDialog();
        }
        else if(connectionStatus.equals("respond"))
        {
            OpenConnectionRespondDialog();
        }
        else if(connectionStatus.equals("connected"))
        {
            OpenRemoveConnectionConfirmDialog();
        }
        else
        {
            HashMap connectionDiaryMap1 = new HashMap();
            connectionDiaryMap1.put("connectionStatus", "respond");
            connectionsDatabaseReference.child(intentUserID).child(currentUserID).updateChildren(connectionDiaryMap1);

            HashMap connectionDiaryMap2 = new HashMap();
            connectionDiaryMap2.put("connectionStatus", "requested");
            connectionsDatabaseReference.child(currentUserID).child(intentUserID).updateChildren(connectionDiaryMap2);

            Toast.makeText(getContext(), "Requested", Toast.LENGTH_SHORT).show();
        }
    }




    private void OpenRemoveRequestedConnectionConfirmDialog()
    {
        final Dialog removeRequestedConnectionConfirmDialog = new Dialog(getContext());
        removeRequestedConnectionConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeRequestedConnectionConfirmDialog.setContentView(R.layout.confirm_layout);
        removeRequestedConnectionConfirmDialog.setTitle("Remove Requested Connection Dialog");
        removeRequestedConnectionConfirmDialog.show();

        TextView title = (TextView)removeRequestedConnectionConfirmDialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Remove Request");

        TextView description = (TextView)removeRequestedConnectionConfirmDialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to remove request with "+username+"?");

        Button nobtn = (Button)removeRequestedConnectionConfirmDialog.findViewById(R.id.confirm_dialog_no_button);
        nobtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeRequestedConnectionConfirmDialog.cancel();
            }
        });

        Button yesbtn = (Button)removeRequestedConnectionConfirmDialog.findViewById(R.id.confirm_dialog_yes_button);
        yesbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(currentUserID).child(intentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(intentUserID).child(currentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                profileReqConnectionBtn.setText("Connect");
                connectionStatus ="null";

                removeRequestedConnectionConfirmDialog.cancel();
            }
        });
    }




    private void OpenRemoveConnectionConfirmDialog()
    {
        final Dialog removeConnectionConfirmDialog = new Dialog(getContext());
        removeConnectionConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeConnectionConfirmDialog.setContentView(R.layout.confirm_layout);
        removeConnectionConfirmDialog.setTitle("Remove Connection Dialog");
        removeConnectionConfirmDialog.show();

        TextView title = (TextView)removeConnectionConfirmDialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Remove Connection");

        TextView description = (TextView)removeConnectionConfirmDialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to remove connection with "+username+"?");

        Button nobtn = (Button)removeConnectionConfirmDialog.findViewById(R.id.confirm_dialog_no_button);
        nobtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeConnectionConfirmDialog.cancel();
            }
        });

        Button yesbtn = (Button)removeConnectionConfirmDialog.findViewById(R.id.confirm_dialog_yes_button);
        yesbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(currentUserID).child(intentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(intentUserID).child(currentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                profileReqConnectionBtn.setText("Connect");
                connectionStatus ="null";

                removeConnectionConfirmDialog.cancel();
            }
        });
    }



    private void OpenConnectionRespondDialog()
    {
        final Dialog connectionRespondDialog = new Dialog(getContext());
        connectionRespondDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        connectionRespondDialog.setContentView(R.layout.connection_respond_layout);
        connectionRespondDialog.setTitle("Connection Respond Window");
        connectionRespondDialog.show();
        Window connectionRespondWindow = connectionRespondDialog.getWindow();
        connectionRespondWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        CircleImageView layoutUserimage = (CircleImageView) connectionRespondDialog.findViewById(R.id.connection_respond_userimage);
        if(!TextUtils.isEmpty(userImage))
        {
            Picasso.with(getContext()).load(userImage).placeholder(R.drawable.profile_image_placeholder).into(layoutUserimage);
        }

        TextView layoutUsername = (TextView) connectionRespondDialog.findViewById(R.id.connection_respond_username);
        if(!TextUtils.isEmpty(username))
        {
            layoutUsername.setText(username);
        }

        Button acceptBtn = (Button) connectionRespondDialog.findViewById(R.id.connection_respond_accept_button);
        Button rejectBtn = (Button) connectionRespondDialog.findViewById(R.id.connection_respond_reject_button);

        acceptBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HashMap connectionDiaryMap1 = new HashMap();
                connectionDiaryMap1.put("connectionStatus", "connected");
                connectionsDatabaseReference.child(intentUserID).child(currentUserID).updateChildren(connectionDiaryMap1);

                HashMap connectionDiaryMap2 = new HashMap();
                connectionDiaryMap2.put("connectionStatus", "connected");
                connectionsDatabaseReference.child(currentUserID).child(intentUserID).updateChildren(connectionDiaryMap2);

                connectionRespondDialog.cancel();
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(currentUserID).child(intentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                cancelConnectionsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Connections").child(intentUserID).child(currentUserID);
                cancelConnectionsDatabaseReference.removeValue();
                profileReqConnectionBtn.setText("Connect");
                connectionStatus ="null";

                connectionRespondDialog.cancel();
            }
        });
    }


    private void RetrieveUserDetails()
    {
        userDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("userimage"))
                    {
                        userImage = dataSnapshot.child("userimage").getValue().toString();
                        if(!TextUtils.isEmpty(userImage))
                        {
                            Picasso.with(getContext()).load(userImage).placeholder(R.drawable.profile_image_placeholder).into(profileUserImage);
                        }
                    }

                    if(dataSnapshot.hasChild("username"));
                    {
                        username = dataSnapshot.child("username").getValue().toString();
                        if(!TextUtils.isEmpty(username))
                        {
                            profileUsername.setText(username);
                        }
                    }


                    if(dataSnapshot.hasChild("useraccounttype"));
                    {
                        accountType = dataSnapshot.child("useraccounttype").getValue().toString();
                        if(accountType.equals("SELL A SERVICE"))
                        {
                            servicesBtn.setVisibility(View.VISIBLE);
                            servicesManagerBtn.setVisibility(View.VISIBLE);

                            if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
                            {
                                servicesManagerBtn.setVisibility(View.GONE);
                            }

                            if(dataSnapshot.hasChild("userposition"))
                            {
                                position = dataSnapshot.child("userposition").getValue().toString();
                                if(!TextUtils.isEmpty(position))
                                {
                                    profilePosition.setText(position+" at iFit");
                                    profilePosition.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("userjoineddate"));
                    {
                        joinedDate = dataSnapshot.child("userjoineddate").getValue().toString();
                        if(!TextUtils.isEmpty(joinedDate))
                        {
                            profileJoinedDate.setText("Joined "+joinedDate);
                        }
                    }


                    if(dataSnapshot.hasChild("usercurrentweight"))
                    {
                        currentWeight = dataSnapshot.child("usercurrentweight").getValue().toString();
                        if(!TextUtils.isEmpty(currentWeight))
                        {
                            profileWeight.setText(" • Weight - "+currentWeight+" kg • ");
                        }
                    }
                    else
                    {
                        profileWeight.setVisibility(View.GONE);
                    }



                    if(dataSnapshot.hasChild("userheight"))
                    {
                        height = dataSnapshot.child("userheight").getValue().toString();
                        if(!TextUtils.isEmpty(height))
                        {
                            profileHeight.setText(" • Height - "+height+" cm • ");
                        }
                    }
                    else
                    {
                        profileHeight.setVisibility(View.GONE);
                    }


                    if(dataSnapshot.hasChild("useractivitylevel"))
                    {
                        activityLevel = dataSnapshot.child("useractivitylevel").getValue().toString();
                        if(!TextUtils.isEmpty(activityLevel))
                        {
                            profileActivityLevel.setText(" • Activity Level - "+activityLevel+" • ");
                        }
                    }
                    else
                    {
                        profileActivityLevel.setVisibility(View.GONE);
                    }




                    if(dataSnapshot.hasChild("userbyear") && dataSnapshot.hasChild("userbmonth") &&
                            dataSnapshot.hasChild("userbday"))
                    {
                        birthday = dataSnapshot.child("userbday").getValue().toString() +" "+dataSnapshot.child("userbmonth").getValue().toString() +" "+
                                dataSnapshot.child("userbyear").getValue().toString();

                        if(!TextUtils.isEmpty(birthday))
                        {
                            profileBirthday.setText(" • "+"Birthday - "+birthday+" • ");
                        }
                    }
                    else
                    {
                        profileBirthday.setVisibility(View.GONE);
                    }



                    if(dataSnapshot.hasChild("usergender"))
                    {
                        gender = dataSnapshot.child("usergender").getValue().toString();
                        if(!TextUtils.isEmpty(gender))
                        {
                            profileGender.setText(" • Gender - "+gender+" • ");
                        }
                    }
                    else
                    {
                        profileGender.setVisibility(View.GONE);
                    }



                    if(dataSnapshot.hasChild("useremail"));
                    {
                        email = dataSnapshot.child("useremail").getValue().toString();
                        if(!TextUtils.isEmpty(email))
                        {
                            profileEmail.setText(email);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        achievementDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("points"))
                    {
                        points = dataSnapshot.child("points").getValue().toString();
                        if(!points.isEmpty())
                        {
                           profilePoints.setText("• "+points+" Points •");
                        }

                        if(Integer.parseInt(points) < 20)
                        {
                            profileBadgeLevel.setText("Silver Member");
                            profileBadge.setImageResource(0);
                            profileBadge.setBackgroundResource(R.drawable.silver_medal_icon);
                        }

                        if(Integer.parseInt(points) >= 20)
                        {
                            profileBadgeLevel.setText("Gold Member");
                            profileBadge.setImageResource(0);
                            profileBadge.setBackgroundResource(R.drawable.gold_medal_icon);
                        }

                        if(Integer.parseInt(points) >= 50)
                        {
                            profileBadgeLevel.setText("Platinum Member");
                            profileBadge.setImageResource(0);
                            profileBadge.setBackgroundResource(R.drawable.platinum_medal_icon);
                        }

                        if(Integer.parseInt(points) >= 100)
                        {
                            profileBadgeLevel.setText("Diamond Member");
                            profileBadge.setImageResource(0);
                            profileBadge.setBackgroundResource(R.drawable.diamond_medal_icon);
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



    /* logout confirm dialog actions */
    private void OpenLogoutConfirmDialog()
    {
        final Dialog logoutconfirmdialog = new Dialog(getContext());
        logoutconfirmdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        logoutconfirmdialog.setContentView(R.layout.confirm_layout);
        logoutconfirmdialog.setTitle("Logout Confirm Dialog");
        logoutconfirmdialog.show();

        TextView title = (TextView)logoutconfirmdialog.findViewById(R.id.confirm_dialog_title);
        title.setText("Logout");

        TextView description = (TextView)logoutconfirmdialog.findViewById(R.id.confirm_dialog_description);
        description.setText("Are you sure you want to logout?");


        final Button nobtn = (Button)logoutconfirmdialog.findViewById(R.id.confirm_dialog_no_button);


        nobtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                logoutconfirmdialog.cancel();
            }
        });


        Button yesbtn = (Button)logoutconfirmdialog.findViewById(R.id.confirm_dialog_yes_button);
        yesbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserLogout();
                logoutconfirmdialog.cancel();
            }
        });
    }



    /* User redirect to welcome page */
    private void UserLogout()
    {
        /* adding Loading bar for 750ms */
        loadingbar = new ProgressDialog(getContext());
        String ProgressDialogMessage="Logging Out...";
        SpannableString spannableMessage=  new SpannableString(ProgressDialogMessage);
        spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
        loadingbar.setMessage(spannableMessage);
        loadingbar.show();
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.setCancelable(false);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mAuth.signOut();
                Intent welcomeIntent = new Intent(getActivity(), WelcomeActivity.class);
                welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(welcomeIntent);
            }
        },750);
    }



    /* User redirect to nutrition page */
    private void UserSendToFoodListPage()
    {
        Intent foodListIntent = new Intent(getActivity(), FoodListActivity.class);
        foodListIntent.putExtra("intentFrom", "ProfileMenu");
        startActivity(foodListIntent);
    }



    private void UserSendToDiaryPage()
    {
        if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            Intent diaryIntent = new Intent(getActivity(), DiaryActivity.class);
            diaryIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
            diaryIntent.putExtra("intentUserID", intentUserID);
            startActivity(diaryIntent);
        }
        else
        {
            Intent diaryIntent = new Intent(getActivity(), DiaryActivity.class);
            diaryIntent.putExtra("intentFrom", "MyProfile");
            diaryIntent.putExtra("intentUserID", currentUserID);
            startActivity(diaryIntent);
        }
    }



    private void UserSendToPostPage()
    {
        if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            Intent postIntent = new Intent(getActivity(), PostActivity.class);
            postIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
            postIntent.putExtra("intentUserID", intentUserID);
            startActivity(postIntent);
        }
        else
        {
            Intent postIntent = new Intent(getActivity(), PostActivity.class);
            postIntent.putExtra("intentFrom", "MyProfile");
            postIntent.putExtra("intentUserID", currentUserID);
            startActivity(postIntent);
        }
    }



    private void UserSendToServicesPage()
    {
        if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            Intent serviceIntent = new Intent(getActivity(), ServiceActivity.class);
            serviceIntent.putExtra("intentFrom", "ViewAnotherUserProfile");
            serviceIntent.putExtra("intentUserID", intentUserID);
            startActivity(serviceIntent);
        }
        else
        {
            Intent serviceIntent = new Intent(getActivity(), ServiceActivity.class);
            serviceIntent.putExtra("intentFrom", "MyProfile");
            serviceIntent.putExtra("intentUserID", currentUserID);
            startActivity(serviceIntent);
        }
    }


    private void UserSendToServicesManagerPage()
    {
        Intent serviceManagerIntent = new Intent(getContext(), ServiceManagerActivity.class);
        serviceManagerIntent.putExtra("intentFrom", "ViewService");
        startActivity(serviceManagerIntent);
    }


    private void UserSendToRequestPage()
    {
        Intent requestsIntent = new Intent(getActivity(), RequestActivity.class);
        startActivity(requestsIntent);
    }

    private void UserSendToOrdersPage()
    {
        Intent orderIntent = new Intent(getActivity(), OrdersActivity.class);
        startActivity(orderIntent);
    }


    private void UserSendToConnectionsPage()
    {
        if(intentPurpose.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            Intent connectionsIntent = new Intent(getActivity(), ConnectionsActivity.class);
            connectionsIntent.putExtra("intentFrom", "OtherUserProfile");
            connectionsIntent.putExtra("intentUserID", intentUserID);
            startActivity(connectionsIntent);
        }
        else
        {
            Intent connectionsIntent = new Intent(getActivity(), ConnectionsActivity.class);
            connectionsIntent.putExtra("intentFrom", "MyProfile");
            connectionsIntent.putExtra("intentUserID", currentUserID);
            startActivity(connectionsIntent);
        }
    }

    private void UserSendToChatPage()
    {
        Intent viewMessagesIntent = new Intent(getActivity(), ViewMessagesActivity.class);
        viewMessagesIntent.putExtra("intentUserID", intentUserID);
        startActivity(viewMessagesIntent);
    }

    private void UserSendToMessagesPage()
    {
        Intent messagesIntent = new Intent(getActivity(), MessagesActivity.class);
        startActivity(messagesIntent);
    }

    private void UserSendToFindPage()
    {
        Intent findIntent = new Intent(getActivity(), FindActivity.class);
        findIntent.putExtra("intentPurpose", "FindUsers");
        startActivity(findIntent);
    }

}
