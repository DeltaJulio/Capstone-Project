package io.github.deltajulio.pantrybank;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;

public class NewItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = "NewItemActivity";
    public static final String NEW = "new";
    public static final String EDIT = "edit";

    private String currentTab;
    private String action;

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
    LinearLayout quantityEnumContainer;
    TextInputLayout quantityLongContainer;
    TextInputEditText quantityLongText;
    AppCompatSpinner quantityEnumSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        currentTab = getIntent().getStringExtra(MainActivity.EXTRA_TAB);
        action = getIntent().getStringExtra(MainActivity.EXTRA_ACTION);

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
        quantitySpinner.setOnItemSelectedListener(this);

        // Pass quantity approximations to spinner
        quantityEnumSpinner = (AppCompatSpinner) findViewById(R.id.quantity_enum_spinner);
        ArrayAdapter<CharSequence> quantityEnumAdapter = ArrayAdapter.createFromResource(this,
                R.array.quantity_approx, android.R.layout.simple_spinner_item);
        quantityEnumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantityEnumSpinner.setAdapter(quantityEnumAdapter);

        // Grab reference to rest of view objects
        nameText = (TextInputEditText) findViewById(R.id.item_name);
        categorySpinner = (AppCompatSpinner) findViewById(R.id.category_spinner);
        pinnedSwitch = (SwitchCompat) findViewById(R.id.pinned_switch);
        quantityLongText = (TextInputEditText) findViewById(R.id.item_quantity);
        quantityEnumContainer = (LinearLayout) findViewById(R.id.quantity_enum_container);
        quantityLongContainer = (TextInputLayout) findViewById(R.id.quantity_long_container);
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
                databaseHandler.GetDatabaseReference()
                        .child(DatabaseHandler.USER_PATH)
                        .child(databaseHandler.GetUserId())
                        .child(DatabaseHandler.CATEGORIES)
                        .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Find category id
                        String category = categorySpinner.getSelectedItem().toString();
                        for (DataSnapshot child : dataSnapshot.getChildren())
                        {
                            if (child.child(Category.NAME).getValue().toString().equals(category))
                            {
                                category = child.child(Category.ID).getValue().toString();
                                break;
                            }
                        }

                        // Get non localized version of QuantityType
                        FoodItem.QuantityType quantityType =
                                FoodItem.QuantityType.values()
                                        [quantitySpinner.getSelectedItemPosition() + 1];

                        String name = nameText.getText().toString();
                        boolean isPinned = pinnedSwitch.isChecked();
                        FoodItem foodItem = new FoodItem(name, isPinned,
                                quantityType, category);

                        // Set quantity
                        if (quantityType == FoodItem.QuantityType.NUMERICAL)
                        {
                            Log.d(TAG, "onOptionsItemSelected:onDataChanged: " + quantityLongText.getText().toString());

                            foodItem.SetQuantityLong(
                                    Long.valueOf(quantityLongText.getText().toString()));
                        } else
                        {
                            FoodItem.QuantityApprox quantityApprox =
                                    FoodItem.QuantityApprox.values()
                                            [quantityEnumSpinner.getSelectedItemPosition() + 1];
                            foodItem.SetQuantityEnum(quantityApprox);
                        }

                        databaseHandler.AddItem(foodItem);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.d(TAG, "onOptionsItemsSelected:onCancelled", databaseError.toException());
                    }
                });

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * OnClickListener bound to our quantity type spinner. Shows the respective input field based
     * on which item was selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // Get non localized version of QuantityType
        FoodItem.QuantityType quantityType =
                FoodItem.QuantityType.values()[position + 1];

        if (quantityType == FoodItem.QuantityType.NUMERICAL)
        {
            quantityEnumContainer.setVisibility(View.GONE);
            quantityLongContainer.setVisibility(View.VISIBLE);
        } else
        {
            quantityEnumContainer.setVisibility(View.VISIBLE);
            quantityLongContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // intentionally left blank
    }
}
