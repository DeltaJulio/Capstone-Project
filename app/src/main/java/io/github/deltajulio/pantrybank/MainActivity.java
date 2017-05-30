package io.github.deltajulio.pantrybank;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.github.deltajulio.pantrybank.auth.LoginActivity;
import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;

public class MainActivity extends AppCompatActivity implements MainFragmentListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_ACTION = "io.github.deltajulio.pantrybank.ACTION";
    public static final String FOOD_ITEM = "io.github.deltajulio.pantrybank.FOOD_ITEM";

    private static boolean firstRun = true;

    /**
     * Firebase Objects
     */
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseHandler databaseHandler;

    /**
     * View Objects
     */
    TabLayout tabLayout;
    TabItem pantryTab;
    TabItem listTab;
    // unimplemented
    //TabItem recipeTab;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        pantryTab = (TabItem) findViewById(R.id.tab_pantry);
        listTab = (TabItem) findViewById(R.id.tab_list);
        // unimplemented
        //recipeTab = (TabItem) findViewById(R.id.tab_recipes);

        // Enable data persistence
        if (firstRun)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            firstRun = false;
        }

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

                    // Create databaseHandler handler
                    databaseHandler = new DatabaseHandler(auth.getCurrentUser().getUid());

                    // Keep data synced
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference(DatabaseHandler.USER_PATH).child(databaseHandler.GetUserId());
                    userRef.keepSynced(true);

                    // TODO: find a better solution
                    AddDefaultCategory();

                    // Set up view pager
                    ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
                    viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), MainActivity.this));
                    tabLayout.setupWithViewPager(viewPager);
                } else
                {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out:");
                    OpenLoginScreen();
                }
            }
        };

        // setup FAB onclick
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (databaseHandler != null)
                {
                    // Launch FoodListActivity
                    Intent intent = new Intent(MainActivity.this, FoodListActivity.class);
                    startActivity(intent);
                }
            }
        });
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
    public DatabaseHandler GetDatabase()
    {
        return databaseHandler;
    }

    @Override
    public void LaunchEditItemActivity(FoodItem item)
    {
        Intent intent = new Intent(MainActivity.this, NewItemActivity.class);
        intent.putExtra(EXTRA_ACTION, NewItemActivity.EDIT);
        intent.putExtra(FOOD_ITEM, item);
        startActivity(intent);
    }

    private void AddDefaultCategory()
    {
        // Create the "uncategorized" category if it does not exist.
        DatabaseReference ref = databaseHandler.GetDatabaseReference()
                .child(DatabaseHandler.USER_PATH)
                .child(auth.getCurrentUser().getUid())
                .child(DatabaseHandler.CATEGORIES);

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    if (child.child(Category.NAME).getValue().toString()
                            .equals(getResources().getString(R.string.uncategorized)))
                    {
                        return;
                    }
                }

                DatabaseReference ref = databaseHandler.GetDatabaseReference()
                        .child(DatabaseHandler.USER_PATH)
                        .child(auth.getCurrentUser().getUid())
                        .child(DatabaseHandler.CATEGORIES);

                String categoryId = ref.push().getKey();
                Category category = new Category(getResources().getString(R.string.uncategorized),
                        categoryId);

                ref.child(categoryId).setValue(category);

                Log.d(TAG, "AddDefaultCategory:onDataChange: default category created");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "AddDefaultCategory:onCancelled", databaseError.toException());
            }
        });
    }
}
