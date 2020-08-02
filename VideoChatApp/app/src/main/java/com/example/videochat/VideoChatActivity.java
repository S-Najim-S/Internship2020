package com.example.videochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener
{

    private static  String API_Key = "46782154";
    private static String SESSION_ID = "1_MX40Njc4MjE1NH5-MTU5MTU0OTkyOTQwMH50elF3b2ZOTHR2Z2YwOTlyT25OWXZEeUV-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njc4MjE1NCZzaWc9MzU1ZGI0ZmQwMGM5OGQ5YTdkNzBmZTBiYjU1N2VhY2VkMmU4MjViMjpzZXNzaW9uX2lkPTFfTVg0ME5qYzRNakUxTkg1LU1UVTVNVFUwT1RreU9UUXdNSDUwZWxGM2IyWk9USFIyWjJZd09UbHlUMjVPV1haRWVVVi1mZyZjcmVhdGVfdGltZT0xNTkxNTQ5OTk3Jm5vbmNlPTAuMTczNzEyMzY3MjI5Mzc3NjYmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MjE1NDc5NiZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(userID).hasChild("Ringing")){

                            userRef.child(userID).child("Ringing").removeValue();
                            if (mPublisher != null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Calling")){

                            userRef.child(userID).child("Calling").removeValue();
                            if (mPublisher != null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }else{
                            if (mPublisher != null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError databaseError) {

                    }
                });
            }
        });
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission(){
        // Checking permissions for multiple things so using an array of string

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)){

            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscribe_container);

            //1. Initialize and connect to the session

            mSession = new Session.Builder(this, API_Key, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }else{
            EasyPermissions.requestPermissions(this, "Please allow the Camera and Mic to use this app", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    // 2.Publishing a stream to the session
    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){

            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);

        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");

    }

    // 3. Subscribing to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream received");

        if (mSubscriber == null){

            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null){
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");

    }
}
