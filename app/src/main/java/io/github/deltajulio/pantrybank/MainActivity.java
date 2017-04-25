package io.github.deltajulio.pantrybank;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.data.Database;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;

public class MainActivity extends AppCompatActivity implements MainFragmentListener
{
    private static final String TAG = "MainActivity";

    /**
     * Firebase Objects
     */
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    /**
     * View Objects
     */
    TabLayout tabLayout;
    TabItem pantryTab;
    TabItem listTab;
    TabItem recipeTab;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        pantryTab = (TabItem) findViewById(R.id.tab_pantry);
        listTab = (TabItem) findViewById(R.id.tab_list);
        recipeTab = (TabItem) findViewById(R.id.tab_recipes);

        // Verify that the user is logged in
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null)
        {
            // User is not logged in, start the login activity
            Log.d(TAG, "onCreate: User is NOT logged in");
            OpenLoginScreen();
        } else
        {
            // User is logged in
            Log.d(TAG, "onCreate:" + auth.getCurrentUser().getEmail());
        }

        authListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:logged_in:" + user.getUid());
                } else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out:");
                    OpenLoginScreen();
                }
            }
        };

        // Set up view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), MainActivity.this));
        tabLayout.setupWithViewPager(viewPager);

        // DEBUG
        Database.AddItem();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (authListener != null)
        {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void OpenLoginScreen()
    {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void OnEdit()
    {
        // TODO: open edit item screen
    }
}