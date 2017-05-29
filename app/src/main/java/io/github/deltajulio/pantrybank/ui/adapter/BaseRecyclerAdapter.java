package io.github.deltajulio.pantrybank.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.TreeMap;

import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * Base class for our adapters.
 */
public abstract class BaseRecyclerAdapter
	extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final String TAG = BaseRecyclerAdapter.class.getSimpleName();

	// Firebase objects
	private Query categoriesRef;
	private Query itemsRef;
	private ChildEventListener categoryListener;
	private ChildEventListener itemListener;

	// Stored copy of our categories, used to sort data by category name
	private HashMap<String, Category> allCategories;
	// Categories that are VISIBLE. (has corresponding FoodItems that are also visible)
	private HashMap<String, Category> visibleCategories;
	// List of food items, to be used by the adapter
	private TreeMap<FoodKey, FoodItem> sortedFood;

	// Category vars
	private static final int categoryResourceId = R.layout.category;
	protected static final int CATEGORY_TYPE = 2;
	protected static final int FOOD_TYPE = 1;
	private final Context context;

	protected BaseRecyclerAdapter(Query categoriesRef, Query itemsRef, Context context)
	{
		this.categoriesRef = categoriesRef;
		this.itemsRef = itemsRef;
		this.context = context;
		allCategories = new HashMap<>();
		visibleCategories = new HashMap<>();
		sortedFood = new TreeMap<>();

		// Monitor changes to allCategories
		categoryListener = new ChildEventListener()
		{
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// add new category to containers
				allCategories.put(category.getCategoryId(), category);
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// update stored version of category
				allCategories.put(category.getCategoryId(), category);
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot)
			{
				// create Category from data snapshot
				Category category = dataSnapshot.getValue(Category.class);
				// remove category from container
				allCategories.remove(category.getCategoryId());
				visibleCategories.remove(category.getCategoryId());
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
				Category category = allCategories.get(categoryId);
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

				UpdateCategoryVisibility(item.getCategoryId());
				notifyDataSetChanged();
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s)
			{
				// Create FoodItem from snapshot
				FoodItem item = dataSnapshot.getValue(FoodItem.class);

				// check if old key needs to be replaced
				for (FoodItem itemItr : sortedFood.values())
				{
					if (itemItr.getFoodId().equals(item.getFoodId()))
					{
						// if category/name has been changed, replace key
						if (!itemItr.getCategoryId().equals(item.getCategoryId()) ||
								!itemItr.getName().equals(item.getName()))
						{
							// Remove item from container
							Pair<Boolean, String> result = GetCategoryName(itemItr.getCategoryId());
							if (!result.first)
							{
								throw null;
							}
							sortedFood.remove(new FoodKey(result.second, itemItr.getName()));
						}
						break;
					}
				}

				if (!ShouldBeDisplayed(item))
				{
					// Remove updated item
					Pair<Boolean, String> result = GetCategoryName(item.getCategoryId());
					if (!result.first)
					{
						throw null;
					}

					sortedFood.remove(new FoodKey(result.second, item.getName()));
					UpdateCategoryVisibility(item.getCategoryId());
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

				UpdateCategoryVisibility(item.getCategoryId());
				notifyDataSetChanged();
			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot)
			{
				// Create FoodItem from snapshot
				FoodItem item = dataSnapshot.getValue(FoodItem.class);
				// Remove from container
				sortedFood.remove(new FoodKey(null, item.getName()));

				UpdateCategoryVisibility(item.getCategoryId());
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
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == CATEGORY_TYPE)
		{
			final View view = LayoutInflater.from(context).inflate(categoryResourceId, parent, false);
			return new CategoryHolder(view);
		} else
		{
			// This should never be reachable code, inherited classes should only call this function
			// if a category header should be made.
			throw null;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
			throws NullPointerException
	{
		PositionResult result = GetObjectAtPosition(position);
		((CategoryHolder)holder).title.setText(result.category.getName());
	}

	@Override
	public int getItemViewType(int position)
	{
		if (IsCategoryPosition(position))
		{
			return CATEGORY_TYPE;
		} else
		{
			return FOOD_TYPE;
		}
	}

	@Override
	public int getItemCount()
	{
		return sortedFood.size() + visibleCategories.size();
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
	 * Called from ChildEventListeners. This function determines whether the Category attached to the
	 * provided ID should be displayed in the RecyclereView.
	 *
	 * @param categoryId CategoryId to be evaluated.
	 * @throws NullPointerException In the event that no such category exists, throw a NPE. This would
	 * be a fatal error.
	 */
	private void UpdateCategoryVisibility(String categoryId) throws NullPointerException
	{
		Category category = allCategories.get(categoryId);

		for (FoodItem foodItem : sortedFood.values())
		{
			if (foodItem.getCategoryId().equals(categoryId))
			{
				visibleCategories.put(categoryId, category);
				return;
			}
		}

		visibleCategories.remove(categoryId);
	}

	protected final boolean IsCategoryPosition(int position)
	{
		int i = 0;
		// iterate through categories
		for (String categoryKey : visibleCategories.keySet())
		{
			String categoryId = visibleCategories.get(categoryKey).getCategoryId();

			// check if position is a Category
			if (i == position)
			{
				return true;
			}
			// iterate through FoodItems
			for (FoodItem foodItem : sortedFood.values())
			{
				// only count items under the current category
				if (foodItem.getCategoryId().equals(categoryId))
				{
					i++;
					// check if position is a FoodItem
					if (i == position)
					{
						return false;
					}
				}
			}

			i++;
		}

		return false;
	}

	protected final PositionResult GetObjectAtPosition(int position)
	{
		int i = 0;
		// iterate through categories
		for (String categoryKey : visibleCategories.keySet())
		{
			String categoryId = visibleCategories.get(categoryKey).getCategoryId();

			// check if position is a Category
			if (i == position)
			{
				return new PositionResult(visibleCategories.get(categoryKey));
			}
			// iterate through FoodItems
			for (FoodItem foodItem : sortedFood.values())
			{
				// only count items under the current category
				if (foodItem.getCategoryId().equals(categoryId))
				{
					i++;
					// check if position is a FoodItem
					if (i == position)
					{
						return new PositionResult(foodItem);
					}
				}
			}

			i++;
		}

		Log.d(TAG, String.valueOf(visibleCategories.size()));

		throw null;
	}

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

	private class CategoryHolder extends RecyclerView.ViewHolder
	{
		private static final int textResourceId = R.id.category_text;

		public final TextView title;

		private CategoryHolder(View view)
		{
			super(view);
			title = (TextView) view.findViewById(textResourceId);
		}
	}

	protected final class PositionResult
	{
		public final boolean isCategory;
		public final Category category;
		public final FoodItem foodItem;

		public PositionResult(Category category)
		{
			this.isCategory = true;
			this.category = category;
			this.foodItem = null;
		}

		public PositionResult(FoodItem foodItem)
		{
			this.isCategory = false;
			this.category = null;
			this.foodItem = foodItem;
		}
	}
}
