package io.github.deltajulio.pantrybank.ui.database;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * Base class for our (non-categorized) adapters.
 */

public abstract class BaseRecyclerAdapter<T extends FoodItem, VH extends RecyclerView.ViewHolder>
	extends RecyclerView.Adapter<VH>
{
	private static final String TAG = BaseRecyclerAdapter.class.getSimpleName();

	// Firebase objects
	private Query categoriesRef;
	private Query itemsRef;
	private ChildEventListener categoryListener;
	private ChildEventListener itemListener;

	// Stored copy of our categories, used to sort data by category name
	private HashMap<String, Category> categories;
	// List of food items, to be used by the adapter
	private TreeMap<FoodKey, FoodItem> sortedFood;

	protected BaseRecyclerAdapter(Query categoriesRef, Query itemsRef)
	{
		this.categoriesRef = categoriesRef;
		this.itemsRef = itemsRef;
		categories = new HashMap<>();
		sortedFood = new TreeMap<>();

		// Monitor changes to categories
		categoryListener = new ChildEventListener()
		{
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// add new category to container
				categories.put(category.getCategoryId(), category);
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// update stored version of category
				categories.put(category.getCategoryId(), category);
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// remove category from container
				categories.remove(category.getCategoryId());
			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s)
			{
				// Intentionally left blank
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				Log.e(TAG, databaseError.toString());
			}
		};
		categoriesRef.addChildEventListener(categoryListener);

		// Monitor changes to food items
		itemListener = new ChildEventListener()
		{
			/**
			 * Searches for the category name, given the provided ID.
			 *
			 * @param categoryId The ID of the category being searched for.
			 * @return First returns TRUE if a match was found, with second being the name of the match.
			 * First returns FALSE if no match was found, with second being null.
			 */
			private Pair<Boolean, String> GetCategoryName(String categoryId)
			{
				Category category = categories.get(categoryId);
				if (category != null)
				{
					return Pair.create(true, category.getName());
				}

				return Pair.create(false, null);
			}

			@Override
			public void onChildAdded(final DataSnapshot dataSnapshot, final String s)
			{
				// create FoodItem from data snapshot
				FoodItem item = dataSnapshot.getValue(FoodItem.class);
				if (!ShouldBeDisplayed(item))
				{
					return;
				}

				// find category name
				Pair<Boolean, String> result = GetCategoryName(item.getCategoryId());
				if (result.first)
				{
					// add to container
					sortedFood.put(new FoodKey(result.second, item.getName()), item);
				} else
				{
					// category was not found, delay (wait for database to finish syncing)
					Log.w(TAG, "itemListener:onChildAdded: category was not found. Retrying!");
					Handler handler = new Handler();
					handler.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							onChildAdded(dataSnapshot, s);
						}
					}, 100);
					return;
				}

				notifyDataSetChanged();
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s)
			{
				// Create FoodItem from snapshot
				FoodItem item = dataSnapshot.getValue(FoodItem.class);
				// Remove item from container
				sortedFood.remove(new FoodKey("", item.getName()));

				if (!ShouldBeDisplayed(item))
				{
					// Do not add updated item
					notifyDataSetChanged();
					return;
				}

				// Find Category name
				Pair<Boolean, String> result = GetCategoryName(item.getCategoryId());
				if (result.first)
				{
					// Update value in container
					sortedFood.put(new FoodKey(result.second, item.getName()), item);
				} else
				{
					// Category was not found. Critical error.
					Log.e(TAG, "itemListener:onChildChanged: category was not found. Data will NOT be updated!");
				}

				notifyDataSetChanged();
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot)
			{
				// Create FoodItem from snapshot
				FoodItem item = dataSnapshot.getValue(FoodItem.class);
				// Remove from container
				sortedFood.remove(new FoodKey(null, item.getName()));

				notifyDataSetChanged();
			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s)
			{
				// Intentionally left blank
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				Log.e(TAG, databaseError.toString());
			}
		};
		itemsRef.addChildEventListener(itemListener);


	}

	/**
	 * Called when the containing fragment is ending its lifecycle. At this point we clean up our
	 * data event listeners.
	 */
	public void RemoveListeners()
	{
		categoriesRef.removeEventListener(categoryListener);
		itemsRef.removeEventListener(itemListener);
	}

	@Override
	public int getItemCount()
	{
		return sortedFood.size();
	}

	protected FoodItem GetFoodItem(int position)
	{
		int i = 0;
		for (Map.Entry<FoodKey, FoodItem> entry : sortedFood.entrySet())
		{
			if (position == i)
			{
				return entry.getValue();
			}
			i++;
		}

		throw null;
	}

	/**
	 * Called from ChildEventListener itemListener, this function determines whether the provided
	 * FoodItem should be displayed in the RecyclerView.
	 *
	 * @param item FoodItem to be evaluated.
	 * @return Return TRUE if FoodItem should be displayed in Recycler view, FALSE if not.
	 */
	protected abstract boolean ShouldBeDisplayed(FoodItem item);

	/**
	 *
	 */
	private class FoodKey implements Comparable<FoodKey>
	{
		public String categoryName;
		public String foodName;

		public FoodKey(String categoryName, String foodName)
		{
			this.categoryName = categoryName;
			this.foodName = foodName;
		}

		@Override
		public int compareTo(@NonNull FoodKey o)
		{
			int categoryCompare = categoryName.compareTo(o.categoryName);
			if (categoryCompare != 0)
			{
				return categoryCompare;
			} else
			{
				return foodName.compareTo(o.foodName);
			}
		}
	}
}
