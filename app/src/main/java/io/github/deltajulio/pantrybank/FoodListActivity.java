package io.github.deltajulio.pantrybank;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.TreeMap;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;
import io.github.deltajulio.pantrybank.ui.adapter.BaseRecyclerAdapter;
import io.github.deltajulio.pantrybank.ui.adapter.FoodListAdapter;

import static io.github.deltajulio.pantrybank.MainActivity.EXTRA_ACTION;
import static io.github.deltajulio.pantrybank.MainActivity.FOOD_ITEM;

/**
 * Displays a list of all FoodItems stored in the database.
 * Clicked items will be launched with NewItemActivity in edit mode.
 */

public class FoodListActivity extends AppCompatActivity implements MainFragmentListener
{
	private static final String TAG = FoodListActivity.class.getSimpleName();

	// Firebase objects
	Query categoriesRef;
	Query itemsRef;
	DatabaseHandler databaseHandler;

	// View Objects
	private RecyclerView recyclerView;

	// Sorted copy of Categories. Sorted by name.
	private TreeMap<String, Category> allCategories;
	// Sorted copy of FoodItems.
	private TreeMap<FoodKey, FoodItem> sortedFood;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_food_list);
		allCategories = new TreeMap<>();
		sortedFood = new TreeMap<>();

		// Initialize view objects
		recyclerView = (RecyclerView) findViewById(R.id.food_list);

		DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
		databaseHandler = null;
		try
		{
			databaseHandler = new DatabaseHandler(FirebaseAuth.getInstance()
					.getCurrentUser().getUid());
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}

		// Get Categories
		categoriesRef = reference.child(DatabaseHandler.USER_PATH)
				.child(databaseHandler.GetUserId())
				.child(DatabaseHandler.CATEGORIES)
				.orderByChild(Category.NAME);
		// Get FoodItems
		itemsRef= reference.child(DatabaseHandler.USER_PATH)
				.child(databaseHandler.GetUserId())
				.child(DatabaseHandler.ITEMS)
				.orderByChild(FoodItem.CATEGORY_ID);

		// Add categories to sorted array from database.
		categoriesRef.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				for (DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					Category category = snapshot.getValue(Category.class);
					if (category == null)
					{
						throw null;
					}

					allCategories.put(category.getName(), category);
				}

				// Add FoodItems to sorted array from database
				itemsRef.addListenerForSingleValueEvent(new ItemEventListener());
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				throw databaseError.toException();
			}
		});
	}

	private void PopulateListView()
	{
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
		BaseRecyclerAdapter recyclerAdapter = new FoodListAdapter(categoriesRef, itemsRef, this, this);
		recyclerView.setAdapter(recyclerAdapter);
	}

	@Override
	public DatabaseHandler GetDatabase() { return databaseHandler; }

	@Override
	public void LaunchEditItemActivity(FoodItem item)
	{
		Intent intent = new Intent(FoodListActivity.this, NewItemActivity.class);
		intent.putExtra(EXTRA_ACTION, NewItemActivity.EDIT);
		intent.putExtra(FOOD_ITEM, item);
		startActivity(intent);
	}

	private class ItemEventListener implements ValueEventListener
	{
		/**
		 * Searches for the category name, given the provided ID
		 * @param categoryId The ID of the category being searched for.
		 * @return First returns TRUE if a match was found, with second being the name of the match.
		 * First returns FALSE if no match was found with second being found.
		 */
		private Pair<Boolean, String> GetCategoryName(String categoryId)
		{
			for (Category category : allCategories.values())
			{
				if (category.getCategoryId().equals(categoryId))
				{
					return Pair.create(true, category.getName());
				}
			}

			return Pair.create(false, null);
		}

		@Override
		public void onDataChange(DataSnapshot dataSnapshot)
		{
			for (DataSnapshot snapshot : dataSnapshot.getChildren())
			{
				FoodItem item = snapshot.getValue(FoodItem.class);
				if (item == null)
				{
					throw null;
				}
				Pair<Boolean, String> result = GetCategoryName(item.getCategoryId());
				if (!result.first) throw null;

				FoodKey key = new FoodKey(result.second, item.getName());
				sortedFood.put(key, item);
			}

			PopulateListView();
		}

		@Override
		public void onCancelled(DatabaseError databaseError)
		{
			throw databaseError.toException();
		}
	}
}
