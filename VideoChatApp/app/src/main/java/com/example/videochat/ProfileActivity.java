package com.example.videochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserID = "";
    private String receiverUserImage = "";
    private String receiverUserName = "";

    private ImageView  background_profile_image;
    private TextView name_profile;
    private Button add_friend, decline_friend;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState = "new";
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();

        background_profile_image = findViewById(R.id.backgroung_profile_view);
        name_profile = findViewById(R.id.name_profile);
        add_friend = findViewById(R.id.accept_friend_request);
        decline_friend = findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_image);
        name_profile.setText(receiverUserName);
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friends Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)){

                            String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if (requestType.equals("sent")){

                                currentState = "request_sent";
                                add_friend.setText("Cancel friend Request");

                            }
                            else if (requestType.equals("received")){

                                currentState = "request_received";
                                add_friend.setText("Accept friend Request");

                                decline_friend.setVisibility(View.VISIBLE);
                                decline_friend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        } else{
                            contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserID)){
                                        currentState = "friends";
                                        add_friend.setText("Delete Contact");

                                    } else {
                                        currentState = "new";
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        // If current user and the reciever is the same then hide the add friend button.
        if(senderUserId.equals(receiverUserID)){

            add_friend.setVisibility(View.GONE);
        }
        else {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If we havent sent a request then the value of current state will be new and we send request

                    if (currentState.equals("new")){
                        sendFriendRequest();

                    } if (currentState.equals("request_sent")){

                        CancelFriendRequest();
                    }
                     if (currentState.equals("request_received")){

                         AcceptFriendRequest();

                    }
                     if (currentState.equals("request_sent")){

                         CancelFriendRequest();

                    }

                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(senderUserId).child(receiverUserID).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(senderUserId).child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                friendRequestRef.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            //remove the request from receiver also
                                                            friendRequestRef.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        //remove the request from receiver also
                                                                        currentState = "friends";
                                                                        add_friend.setText("Delete Contact");
                                                                        decline_friend.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //remove the request from receiver also
                    friendRequestRef.child(receiverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //remove the request from receiver also
                                currentState = "new";
                                add_friend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {

        friendRequestRef.child(senderUserId).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // The receiver will know who the is the sender of request.
                    friendRequestRef.child(receiverUserID).child(senderUserId).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        currentState = "request_sent";
                                        add_friend.setText("Cancel friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });
    }
}
