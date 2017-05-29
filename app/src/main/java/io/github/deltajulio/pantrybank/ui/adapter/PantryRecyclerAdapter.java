package io.github.deltajulio.pantrybank.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.firebase.database.Query;

import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;
import io.github.deltajulio.pantrybank.ui.PantryFoodHolder;
import io.github.deltajulio.pantrybank.ui.PantryOnClickListener;

public class PantryRecyclerAdapter extends BaseRecyclerAdapter implements PantryOnClickListener
{
	private static final String TAG = PantryRecyclerAdapter.class.getSimpleName();
	private final DatabaseHandler databaseHandler;
	private final MainFragmentListener mainListener;

	public PantryRecyclerAdapter(Query categoriesRef, Query itemsRef,
	                             MainFragmentListener onClickListener, Context context)
	{
		super(categoriesRef, itemsRef, context);

		this.mainListener = onClickListener;
		databaseHandler = onClickListener.GetDatabase();
	}

	/**
	 * @param item FoodItem to be evaluated.
	 * @return TRUE if the item is pinned OR the quantity is NOT 0. False otherwise.
	 */
	@Override
	protected boolean ShouldBeDisplayed(FoodItem item)
	{
		if (item.getIsPinned())
		{
			return true;
		}

		if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
		{
			if (Integer.valueOf(item.getQuantity()) == 0)
			{
				return false;
			}
		} else // enum quantity type
		{
			if (FoodItem.QuantityApprox.valueOf(item.getQuantity()) ==
					FoodItem.QuantityApprox.NONE_REMAINING)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == FOOD_TYPE)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_item, parent, false);
			return new PantryFoodHolder(view);
		} else
		{
			return super.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
			throws NullPointerException
	{
		if (IsCategoryPosition(position))
		{
			super.onBindViewHolder(holder, position);
			return;
		}

		FoodItem item = GetObjectAtPosition(position).foodItem;

		PantryFoodHolder foodHolder = (PantryFoodHolder) holder;
		foodHolder.SetOnClickListener(this);
		foodHolder.SetItemName(item.getName());
		foodHolder.SetIsPinned(item.getIsPinned());

		if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
		{
			foodHolder.SetItemQuantity(String.valueOf(item.GetQuantityLong()));
		} else
		{
			foodHolder.SetItemQuantity(item.GetQuantityEnum().toString());
		}
	}

	@Override
	public void OnPinClicked(int position) throws NullPointerException
	{
		FoodItem item = GetObjectAtPosition(position).foodItem;
		databaseHandler.UpdateIsPinned(item.getFoodId(), !item.getIsPinned());
	}

	@Override
	public void OnEditClicked(int position)
	{
		FoodItem item = GetObjectAtPosition(position).foodItem;

		// Launch new item activity in edit mode
		mainListener.LaunchEditItemActivity(item);
	}
}