package io.github.deltajulio.pantrybank;

import android.os.Handler;
import android.support.annotation.IntegerRes;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

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
	AppCompatSpinner categorySpinner;
	SwitchCompat pinnedSwitch;
	TextInputLayout quantityContainer;
	TextInputEditText quantityText;
	Button deleteButton;
	ImageButton addButton;
	ImageButton subtractButton;

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

		// Grab reference to rest of view objects
		nameText = (TextInputEditText) findViewById(R.id.item_name);
		categorySpinner = (AppCompatSpinner) findViewById(R.id.category_spinner);
		pinnedSwitch = (SwitchCompat) findViewById(R.id.pinned_switch);
		quantityText = (TextInputEditText) findViewById(R.id.item_quantity);
		quantityText.setText("0");
		quantityContainer = (TextInputLayout) findViewById(R.id.quantity_long_container);
		deleteButton = (Button) findViewById(R.id.button_delete);

		// Onclick listeners for incrementing/decrementing quantity
		addButton = (ImageButton) findViewById(R.id.button_add);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String quantityString = quantityText.getText().toString();
				int currentQuantity = quantityString.equals("") ? 0 : Integer.valueOf(quantityString);
				quantityText.setText(String.valueOf(++currentQuantity));
			}
		});
		subtractButton = (ImageButton) findViewById(R.id.button_subtract);
		subtractButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String quantityString = quantityText.getText().toString();
				int currentQuantity = quantityString.equals("") ? 0 : Integer.valueOf(quantityString);
				int newQuantity = --currentQuantity >= 0 ? currentQuantity : 0;
				quantityText.setText(String.valueOf(newQuantity));
			}
		});

		if (intentAction.equals(EDIT))
		{
			// Change activity title based on bundle extras
			actionBar.setTitle(R.string.title_activity_edit_item);

			// fill input fields with info from database
			PopulateInputFields();

			// make the delete button visible
			deleteButton.setVisibility(View.VISIBLE);

			// set up on click listener for delete button
			deleteButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// TODO: prompt for confirmation
					FoodItem foodItem = (FoodItem) getIntent().getSerializableExtra(MainActivity.FOOD_ITEM);
					databaseHandler.DeleteItem(foodItem.getFoodId());
					finish();
				}
			});
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
							categoriesDirectory = new ArrayList<DataSnapshot>();

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

		// Populate quantity field
		long quantity = foodItem.GetQuantity();
		quantityText.setText(String.valueOf(quantity));

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
				if (intentAction.equals(NEW))
				{
					SaveNewItem();
				} else if (intentAction.equals(EDIT))
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
	 * Grabs data from input fields and saves it to database as a new item.
	 */
	private void SaveNewItem()
	{
		// find name
		String name = nameText.getText().toString();

		// find cateogory id
		String categoryName = categorySpinner.getSelectedItem().toString();
		String categoryId = null;
		for (DataSnapshot child : categoriesDirectory)
		{
			if (categoryName.equalsIgnoreCase(child.child(Category.NAME).getValue().toString()))
			{
				categoryId = child.child(Category.ID).getValue().toString();
			}
		}

		// find quantity
		String quantityString = quantityText.getText().toString();
		long quantity = quantityString.equals("") ? 0 : Long.valueOf(quantityString);

		//find isPinned
		boolean isPinned = pinnedSwitch.isChecked();

		// create food item
		FoodItem foodItem;
		foodItem = new FoodItem(name, isPinned, quantity, categoryId);

		// send data to db
		databaseHandler.AddItem(foodItem);
		finish(); // return to main activity
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

		// update quantity
		long quantity = Long.valueOf(quantityText.getText().toString());
		if (quantity != foodItem.GetQuantity())
		{
			databaseHandler.UpdateQuantity(foodItem.getFoodId(), quantity);
		}

		/* update category */

		// find category ID
		String categoryName = categorySpinner.getSelectedItem().toString();
		String categoryId = null;
		for (DataSnapshot child : categoriesDirectory)
		{
			if (categoryName.equalsIgnoreCase(child.child(Category.NAME).getValue().toString()))
			{
				categoryId = child.child(Category.ID).getValue().toString();
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

		// return to main activity
		finish();
	}

}
