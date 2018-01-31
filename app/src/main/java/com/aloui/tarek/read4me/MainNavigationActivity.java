package com.aloui.tarek.read4me;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aloui.tarek.read4me.Activities.AboutUsActivity;
import com.aloui.tarek.read4me.Activities.SettingsActivity;
import com.aloui.tarek.read4me.Other.CircleTransform;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    private static final int SPEECH_RECOGNITION_CODE = 124;
    private static final String CONTROLS = "controls";
    private static final String COMMANDS = "commands";
    //FRAGMENT
    private static final String HOMEFRAGMENTTAG = "home_fragment";
    private static final String LOCALLIBFRAGMENTTAG = "local_lib";
    private static final String ONLINELIBFRAGMENTTAG = "online_lib";
    //Command STRINGS
    private static final String VOLUP = "VOLUP";
    private static final String VOLDOWN = "VOLDOWN";
    private static final String NEXTPAGE = "NEXT";
    private static final String PREVPAGE = "PREVIOUS";
    private static final String PAUSE = "PAUSE";
    //Navigation Drawer Items
    Toolbar toolbar;
    DrawerLayout drawer;
    //USER DATA NAVIGATION
    TextView UsernameTV;
    TextView EmailTV;
    ImageView UserImageIV;
    //AUDIO MANAGER
    AudioManager mAudioManager;
    // USER DATA
    Locale lang = Locale.US;
    //Firebase
    private DatabaseReference mFirebaseRef;
    private DatabaseReference mCommands;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //Setting Title
        toolbar.setTitle(R.string.nav_home);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.btn_assistant) {
                    //SET UP ASSISTANT
                    startSpeechToText();
                    return true;
                }

                return false;
            }
        });

        //NAVIGATION DRAWER
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //FRAGMENTS MANAGEMENT
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment frag = new HomeFragment();
        ft.add(R.id.your_placeholder, frag, HOMEFRAGMENTTAG);
        ft.commit();
        navigationView.setCheckedItem(R.id.nav_home);


        //NIVGATION USER DATA
        UsernameTV = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tv_username);
        EmailTV = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tv_email);
        UserImageIV = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.iv_circle_image);

        //AUDIOMANAGER INIT
        mAudioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);

        //FIREBASE CHECK LOGIN
        auth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //user not signed in
                    signInUser(); //LAUNCH FIREBASE SIGN IN FLOW
                } else {
                    // Set UI DATA
                    Log.w("DEBUG", "DATA: " + user.getEmail());
                    UsernameTV.setText(user.getDisplayName());
                    EmailTV.setText(user.getEmail());
                    Glide.with(getApplicationContext()).load(user.getPhotoUrl())
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .transform(new CircleTransform(getApplicationContext()))
                            .into(UserImageIV);


                }
            }
        };

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mCommands = mFirebaseRef.child(CONTROLS).child(COMMANDS);
        mCommands.setValue(""); //NO COMMAND AT FIRST
        mCommands.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String commandStr = dataSnapshot.getValue().toString();
                    Log.w("COMMAND", commandStr);
                    if (commandStr.equals(VOLDOWN)) {
                        Log.w("VOLUME ", String.valueOf(mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC)));
                        int newVol = Math.max(mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC) - 10, 0);
                        mAudioManager.setStreamVolume(mAudioManager.STREAM_MUSIC, newVol, AudioManager.FLAG_PLAY_SOUND);
                    } else if (commandStr.equals(VOLUP)) {
                        Log.w("VOLUME ", String.valueOf(mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC)));
                        int newVol = Math.min(mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC) + 10, mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC));
                        mAudioManager.setStreamVolume(mAudioManager.STREAM_MUSIC, newVol, AudioManager.FLAG_PLAY_SOUND);
                    } else if (commandStr.equals(PAUSE)) {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HOMEFRAGMENTTAG);


                        if (homeFragment != null) {
                            homeFragment.commandPauseResume();
                        }
                    } else if (commandStr.equals(NEXTPAGE)) {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HOMEFRAGMENTTAG);

                        if (homeFragment != null) {
                            homeFragment.nextPage();
                        }
                    } else if (commandStr.equals(PREVPAGE)) {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HOMEFRAGMENTTAG);

                        if (homeFragment != null) {
                            homeFragment.previousPage();
                        }
                    }

                    mCommands.setValue("");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        auth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            // Replace the contents of the container with the new fragment
            Fragment fragment = new HomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment, HOMEFRAGMENTTAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            //Setting Title
            toolbar.setTitle(R.string.nav_home);

        } else if (id == R.id.nav_local_lib) {
            Fragment fragment = new LocalLibFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment, LOCALLIBFRAGMENTTAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            //Setting Title
            toolbar.setTitle(R.string.nav_local_lib);

        } else if (id == R.id.nav_online_lib) {
            Fragment fragment = new OnlineLibFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment, ONLINELIBFRAGMENTTAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            //Setting Title
            toolbar.setTitle(R.string.nav_online_lib);

        } else if (id == R.id.nav_settings) {
            drawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_sign_out) {
            drawer.closeDrawer(GravityCompat.START);
            signOutUser();
            signInUser();

//            Intent intent = new Intent(this, HelpActivity.class);
//            startActivity(intent);

        } else if (id == R.id.nav_about_us) {
            drawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, AboutUsActivity.class);
            startActivity(intent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if(!isUserLoggedIn()){signInUser();} //LAUNCH FIREBASE SIGN IN FLOW
    }

    private void signInUser()
    {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    private void signOutUser()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // TODO GET BACK TO ACTIVITY 1
                    }
                });

    }

    private boolean isUserLoggedIn()
    {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    void read4meAssistant(String query) {
        //API.AI action dermination
        View constraintL = this.findViewById(R.id.constraint_layout_main);
        Snackbar.make(constraintL, query, Snackbar.LENGTH_LONG).show();

        //READ Result or take action
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hey, how can I help you?");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("ACT RESULT", String.valueOf(requestCode));
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    for (int i = 0; i < result.size(); i++) Log.w("VOICE COMMAND", result.get(i));
                    String text = result.get(0);
                    Log.w("VOICE COMMAND", text);
                    read4meAssistant(text);
                }
                break;
        }
    }
}
