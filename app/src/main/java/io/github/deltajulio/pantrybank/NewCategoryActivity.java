package io.github.deltajulio.pantrybank;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;

public class NewCategoryActivity extends AppCompatActivity
{
	private static final String TAG = NewCategoryActivity.class.getSimpleName();

	private DatabaseHandler databasehandler;
	private String oldName;
	private String categoryId;

	private TextInputEditText categoryText;
	private Button deleteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_category);

		// Instantiate databaseHandler
		try
		{
			databasehandler = new DatabaseHandler(FirebaseAuth.getInstance().getCurrentUser().getUid());
		} catch (NullPointerException e)
		{
			Log.e(TAG, e.toString());
			finish();
			return;
		}

		// Set toolbar as app bar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Enable up navigation button on app bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Grab reference to view objects
		categoryText = (TextInputEditText) findViewById(R.id.category_name);
		deleteButton = (Button) findViewById(R.id.button_delete);

		// Text listener to check if user is adding an already saved Category. Instead of creating a new
		// entry of (x), we show the stored name and display a delete button.
		categoryText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// intentionally left blank
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// intentionally left blank
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				final String string = s.toString();
				if (oldName != null && oldName.equals(string))
				{
					return;
				}
				oldName = string;

				DatabaseReference categories = databasehandler.GetCategories();
				categories.addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						for (DataSnapshot child : dataSnapshot.getChildren())
						{
							Category category = child.getValue(Category.class);
							if (category.getName().equalsIgnoreCase(string))
							{
								// match found, set activity to edit mode.
								categoryText.setText(category.getName());
								deleteButton.setVisibility(View.VISIBLE);
								categoryId = category.getCategoryId();
								return;
							}
						}
						// no match found
						deleteButton.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onCancelled(DatabaseError databaseError)
					{
						Log.e(TAG, "onCreate: ", databaseError.toException());
					}
				});
			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				databasehandler.DeleteCategory(categoryId, v);
				Intent intent = new Intent(NewCategoryActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.appbar_new_category, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_done:
			{
				if (deleteButton.getVisibility() == View.VISIBLE)
				{
					UpdateCategory();
				} else
				{
					SaveNewCategory();
				}
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void SaveNewCategory()
	{
		String name = categoryText.getText().toString();

		// create category
		Category category = new Category(name);

		// send data to db
		databasehandler.AddCategory(category);
		finish(); // return to main activity
	}

	private void UpdateCategory()
	{
		Category category = new Category(categoryText.getText().toString(), categoryId);

		databasehandler.UpdateCategoryName(category);
	}
}
