package io.github.deltajulio.pantrybank;

import android.os.Handler;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;

public class NewItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = "NewItemActivity";
    public static final String NEW = "new";
    public static final String EDIT = "edit";

    private String intentAction;
    private ArrayList<DataSnapshot> categoriesDirectory;

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

        intentAction = getIntent().getStringExtra(MainActivity.EXTRA_ACTION);

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

        if (intentAction.equals(EDIT))
        {
            // Change activity title based on bundle extras
            actionBar.setTitle(R.string.title_activity_edit_item);

            // fill input fields with info from database
            PopulateInputFields();
        }
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
                            // Save the entire object to an array. This is used later when adding items to db.
                            categoriesDirectory.add(category);
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

    /**
     * When this activity is launched in edit mode, this populates the input fields to match the
     * already supplied data from the db.
     */
    private void PopulateInputFields()
    {
        if (!intentAction.equals(EDIT))
        {
            Log.w(TAG, "PopulateInputFields: was called in NEW mode. Aborting!");
            return;
        }

        FoodItem foodItem = (FoodItem) getIntent().getSerializableExtra(MainActivity.FOOD_ITEM);

        // Populate name field
        nameText.setText(foodItem.getName());
        // Populate quantity type field
        FoodItem.QuantityType quantityType = foodItem.getQuantityType();
        for (int i = 0; i < quantitySpinner.getAdapter().getCount(); i++)
        {
            if (quantityType.toString().equalsIgnoreCase(quantitySpinner.getAdapter().getItem(i).toString()))
            {
                quantitySpinner.setSelection(i);
            }
        }

        if (quantityType == FoodItem.QuantityType.NUMERICAL)
        {
            // Populate quantity long field
            long quantityLong = foodItem.GetQuantityLong();
            quantityLongText.setText(String.valueOf(quantityLong));
        } else
        {
            // Populate quantity enum field
            String quantityApprox = foodItem.GetQuantityEnum().toString();
            for (int i = 0; i <quantityEnumSpinner.getAdapter().getCount(); i++)
            {
                if (quantityApprox.equalsIgnoreCase
                    (quantityEnumSpinner.getAdapter().getItem(i).toString()))
                {
                    quantityEnumSpinner.setSelection(i);
                }
            }
        }

        // Populate pinned switch
        pinnedSwitch.setChecked(foodItem.getIsPinned());

        /**
         * Select the desired category in categorySpinner. Since FoodItem stores the ID of the
         * category, we need to grab the name from the db. Then we wait for
         * {@link #PopulateCategorySpinner()} to populate the view to find the matching
         * category in the spinner.
         */
        databaseHandler.GetDatabaseReference()
                .child(DatabaseHandler.USER_PATH)
                .child(databaseHandler.GetUserId())
                .child(DatabaseHandler.CATEGORIES)
                .child(foodItem.getCategoryId()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final DataSnapshot finalDatasnapShot = dataSnapshot;

                // We need to delay looking at the categorySpinner to allow time for the adapter to
                // populate the view.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (int i = 0; i < categorySpinner.getAdapter().getCount(); i++)
                        {
                            if (categorySpinner.getAdapter().getItem(i).toString()
                                    .equals(finalDatasnapShot.child(Category.NAME).getValue().toString()))
                            {
                                categorySpinner.setSelection(i);
                            }
                        }
                    }
                }, 100);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "PopulateInputFields:onCancelled", databaseError.toException());
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
                if (intentAction == NEW)
                {
                    SaveNewItem();
                } else if (intentAction == EDIT)
                {
                    UpdateItem();
                }

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

    /**
     * Grabs data from input fields and saves it to database as a new item.
     */
    private void SaveNewItem()
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
    }

	/**
	 * Similar to {@link #SaveNewItem()}, except this updates the db item instead of making a new
	 * one. This also checks which properties need to be updated, to reduce bandwitdh usage.
	 * (e.g. if name was not changed, that value will not be pushed again)
	 */
	private void UpdateItem()
	{
		final FoodItem foodItem = (FoodItem) getIntent().getSerializableExtra(MainActivity.FOOD_ITEM);

		// update name
		String name = nameText.getText().toString();
		if (!name.equals(foodItem.getName()))
		{
			databaseHandler.UpdateName(foodItem.getFoodId(), name);
		}

		// update quantity type
		// Get non localized version of quantity type
		FoodItem.QuantityType quantityTypeEnum = FoodItem.QuantityType.values()
				[quantitySpinner.getSelectedItemPosition() + 1];
		if (!quantityTypeEnum.equals(foodItem.getQuantityType()))
		{
			databaseHandler.UpdateQuantityType(foodItem.getFoodId(), quantityTypeEnum);
		}

		// update quantity
		if (quantityTypeEnum.equals(FoodItem.QuantityType.NUMERICAL))
		{
			long quantity = Long.valueOf(quantityLongText.getText().toString());
			if (quantity != foodItem.GetQuantityLong())
			{
				databaseHandler.UpdateQuantityLong(foodItem.getFoodId(), quantity);
			}
		} else
		{
			FoodItem.QuantityApprox quantity
					= FoodItem.QuantityApprox.valueOf(quantityEnumSpinner.getSelectedItem().toString());
			if (!quantity.equals(foodItem.GetQuantityEnum()))
			{
				databaseHandler.UpdateQuantityEnum(foodItem.getFoodId(), quantity);
			}
		}

		// update category
		// find category ID
		String categoryName = categorySpinner.getSelectedItem().toString();
		String categoryId = null;
		for (DataSnapshot child : categoriesDirectory)
		{
			if (categoryName.equalsIgnoreCase(child.child(Category.NAME).getValue().toString()))
			{
				categoryId = child.child(Category.NAME).getValue().toString();
			}
		}
		if (categoryId != null && !categoryId.equals(foodItem.getCategoryId()))
		{
			databaseHandler.UpdateCategory(foodItem.getFoodId(), categoryId);
		} else if (categoryId == null)
		{
			Log.e(TAG, "UpdateItem: categoryId was NULL");
		}

		// update isPinned
		boolean isPinned = pinnedSwitch.isChecked();
		if (isPinned != foodItem.getIsPinned())
		{
			databaseHandler.UpdateIsPinned(foodItem.getFoodId(), isPinned);
		}
	}

}
