package io.github.deltajulio.pantrybank;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;

public class NewItemActivity extends AppCompatActivity
{
    private static final String TAG = "NewItemActivity";

    /**
     * Firebase Objects
     */
    private DatabaseHandler databaseHandler;

    /**
     * View Objects
     */
    TextInputEditText nameText;
    AppCompatSpinner quantitySpinner;
    AppCompatSpinner categorySpinner;
    SwitchCompat pinnedSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        // Instantiate databaseHandler handler
        try
        {
            databaseHandler = new DatabaseHandler(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (NullPointerException e)
        {
            Log.d(TAG, "onCreate: getCurrentUser() returned NULL");
            finish();
            return;
        }

        // Set toolbar as app bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable up navigation button on app bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Pass categories to spinner
        PopulateCategorySpinner();

        // Pass quantity types to spinner
        quantitySpinner = (AppCompatSpinner) findViewById(R.id.quantity_type_spinner);
        ArrayAdapter<CharSequence> quantityAdapter = ArrayAdapter.createFromResource(this,
                R.array.quantity_types, android.R.layout.simple_spinner_item);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(quantityAdapter);

        // Grab reference to rest of view objects
        nameText = (TextInputEditText) findViewById(R.id.item_name);
        categorySpinner = (AppCompatSpinner) findViewById(R.id.category_spinner);
        pinnedSwitch = (SwitchCompat) findViewById(R.id.pinned_switch);
    }

    private void PopulateCategorySpinner()
    {
        databaseHandler.GetDatabaseReference()
                .child(DatabaseHandler.USER_PATH)
                .child(databaseHandler.GetUserId())
                .child(DatabaseHandler.CATEGORIES)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        ArrayList<String> categories = new ArrayList<>();

                        for (DataSnapshot category : dataSnapshot.getChildren())
                        {
                            categories.add(category.child(Category.NAME).getValue().toString());
                        }

                        // Pass categories to category spinner
                        AppCompatSpinner categorySpinner = (AppCompatSpinner) findViewById(R.id.category_spinner);
                        ArrayAdapter<String> categoryAdapter =
                                new ArrayAdapter<String>(NewItemActivity.this,
                                android.R.layout.simple_spinner_item, categories);
                        categorySpinner.setAdapter(categoryAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.d(TAG, "onCreate:onCancelled", databaseError.toException());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.appbar_new_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_done:
            {
                String name = nameText.getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                FoodItem.QuantityType quantityType =
                        FoodItem.QuantityType.valueOf(quantitySpinner.getSelectedItem().toString()
                        .toUpperCase());
                boolean isPinned = pinnedSwitch.isChecked();
                FoodItem foodItem = new FoodItem(name, isPinned,
                        quantityType, category);

                databaseHandler.AddItem(foodItem);
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
