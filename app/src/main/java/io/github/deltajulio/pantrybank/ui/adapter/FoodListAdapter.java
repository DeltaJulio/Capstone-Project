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

	public FoodListAdapter(Query categoriesRef, Query itemsRef, MainFragmentListener mainListener,
	                       Context context)
	{
		super(categoriesRef, itemsRef, context);
		this.mainListener = mainListener;
		databasehandler = mainListener.GetDatabase();
	}

	@Override
	protected boolean ShouldBeDisplayed(FoodItem item)
	{
		return true;
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
	}

	public void OnDeleteClicked(int position)
	{
		PositionResult result = GetObjectAtPosition(position);
		if (result.foodItem == null)
		{
			Log.e(TAG, "OnDeleteClicked: foodItem was NULL.");
			throw null;
		}

		mainListener.LaunchEditItemActivity(result.foodItem);
	}
}
