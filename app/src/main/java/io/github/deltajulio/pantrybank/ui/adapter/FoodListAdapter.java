package io.github.deltajulio.pantrybank.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.Query;

import java.util.TreeMap;

import io.github.deltajulio.pantrybank.FoodKey;
import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.FoodListHolder;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;

/**
 * TODO: add a class header comment
 */

public class FoodListAdapter extends BaseRecyclerAdapter
{
	private static final String TAG = FoodListAdapter.class.getSimpleName();

	private final DatabaseHandler databasehandler;
	private MainFragmentListener mainListener;
	private String filter;

	public FoodListAdapter(Query categoriesRef, Query itemsRef, MainFragmentListener mainListener,
	                       Context context)
	{
		super(categoriesRef, itemsRef, context);
		this.mainListener = mainListener;
		databasehandler = mainListener.GetDatabase();
		filter = "";
	}

	/**
	 * Sets the string used to check whether a FoodItem should be displayed.
	 */
	public void SetFilter(String filter)
	{
		this.filter = filter;
		AddListeners();
	}

	@Override
	protected boolean ShouldBeDisplayed(FoodItem item)
	{
		return filter.equals("") || item.getName().contains(filter);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == FOOD_TYPE)
		{
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.food_list_item, parent, false);
			return new FoodListHolder(this, view);
		} else
		{
			return super.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
			throws NullPointerException
	{
		if (IsCategoryPosition(visibleCategories, sortedFood, position))
		{
			super.onBindViewHolder(holder, position);
			return;
		}

		FoodItem item = GetObjectAtPosition(position).foodItem;

		FoodListHolder foodHolder = (FoodListHolder) holder;
		foodHolder.SetItemName(item.getName());
		foodHolder.SetOnItemClick();
	}

	public void OnFoodItemClicked(int position)
	{
		PositionResult result = GetObjectAtPosition(position);
		if (result.foodItem == null)
		{
			Log.e(TAG, "OnFoodItemClicked: foodItem was NULL.");
			return;
		}

		mainListener.LaunchEditItemActivity(result.foodItem);
	}

	public void OnDeleteClicked(int position)
	{
		PositionResult result = GetObjectAtPosition(position);
		if (result.foodItem == null)
		{
			Log.e(TAG, "OnDeleteClicked: foodItem was NULL.");
			return;
		}

		String categoryName = null;
		for (Category category : visibleCategories.values())
		{
			if (category.getCategoryId() == result.foodItem.getCategoryId())
			{
				categoryName = category.getName();
			}
		}
		sortedFood.remove(new FoodKey(categoryName, result.foodItem.getName()));
		UpdateCategoryVisibility(result.foodItem.getCategoryId());
		notifyDataSetChanged();
	}
}
