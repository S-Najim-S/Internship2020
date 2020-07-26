package com.example.videochat;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeopleBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);


        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactsList = findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent findPeopleIntent = new Intent(MainActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);

            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.navigation_home:
                            Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            break;

                        case R.id.navigation_settings:
                            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;

                        case R.id.navigation_notifications:
                            Intent notificationsIntent = new Intent(MainActivity.this, NotificationsActivity.class);
                            startActivity(notificationsIntent);
                            break;

                        case R.id.navigation_logout:
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                            startActivity(logoutIntent);
                            finish();
                            break;

                    }
                    return true;
                }
            };

}
