package com.aloui.tarek.read4me;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.aloui.tarek.read4me.Activities.AboutUsActivity;
import com.aloui.tarek.read4me.Activities.SettingsActivity;
import com.aloui.tarek.read4me.Other.CircleTransform;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    //Navigation Drawer Items
    Toolbar toolbar;
    DrawerLayout drawer;
    //USER DATA NAVIGATION
    TextView UsernameTV;
    TextView EmailTV;
    ImageView UserImageIV;
    //Firebase
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
        ft.add(R.id.your_placeholder, frag, frag.getClass().getName());
        ft.commit();
        navigationView.setCheckedItem(R.id.nav_home);


        //NIVGATION USER DATA
        UsernameTV = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tv_username);
        EmailTV = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tv_email);
        UserImageIV = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.iv_circle_image);

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
            ft.replace(R.id.your_placeholder, fragment, fragment.getTag());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            //Setting Title
            toolbar.setTitle(R.string.nav_home);

        } else if (id == R.id.nav_local_lib) {
            Fragment fragment = new LocalLibFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment, fragment.getTag());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            //Setting Title
            toolbar.setTitle(R.string.nav_local_lib);

        } else if (id == R.id.nav_online_lib) {
            Fragment fragment = new OnlineLibFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment, fragment.getTag());
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

}
