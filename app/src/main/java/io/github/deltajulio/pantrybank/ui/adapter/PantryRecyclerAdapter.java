package io.github.deltajulio.pantrybank.ui.adapter;

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
import io.github.deltajulio.pantrybank.ui.database.BaseRecyclerAdapter;

public class PantryRecyclerAdapter
		extends BaseRecyclerAdapter<FoodItem, PantryFoodHolder>
		implements PantryOnClickListener
{
	private static final String TAG = PantryRecyclerAdapter.class.getSimpleName();
	private final DatabaseHandler databaseHandler;
	private final MainFragmentListener mainListener;

	public PantryRecyclerAdapter(Query categoriesRef, Query itemsRef,
	                             MainFragmentListener onClickListener)
	{
		super(categoriesRef, itemsRef);

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
	public PantryFoodHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_item, parent, false);
		return new PantryFoodHolder(view);
	}

	@Override
	public void onBindViewHolder(PantryFoodHolder holder, int position)
	{
		holder.SetOnClickListener(this);
		FoodItem item = GetFoodItem(position);

		holder.SetItemName(item.getName());
		holder.SetIsPinned(item.getIsPinned());

		if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
		{
			holder.SetItemQuantity(String.valueOf(item.GetQuantityLong()));
		} else
		{
			holder.SetItemQuantity(item.GetQuantityEnum().toString());
		}
	}

	@Override
	public void OnPinClicked(int position)
	{
		FoodItem item = GetFoodItem(position);
		databaseHandler.UpdateIsPinned(item.getFoodId(), !item.getIsPinned());
	}

	@Override
	public void OnDropDownClicked(int position, PantryFoodHolder holder)
	{
		ToggleButton dropDown = holder.GetDropDown();
		holder.SetActionListVisibility(dropDown.isChecked() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void OnEditClicked(int position)
	{
		// Launch new item activity in edit mode
		mainListener.LaunchEditItemActivity(GetFoodItem(position));
	}

	@Override
	public void OnAddClicked(int position)
	{
		FoodItem item = GetFoodItem(position);
		{
			if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
			{
				databaseHandler.UpdateQuantityLong(item.getFoodId(), item.GetQuantityLong() + 1);
			} else
			{
				databaseHandler.UpdateQuantityEnum(item.getFoodId(), item.GetQuantityEnum().Next());
			}
		}
	}

	@Override
	public void OnRemoveClicked(int position)
	{
		FoodItem item = GetFoodItem(position);
		if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
		{
			long quantity = item.GetQuantityLong() - 1;
			databaseHandler.UpdateQuantityLong(item.getFoodId(), (quantity >= 0 ? quantity : 0));
		} else
		{
			databaseHandler.UpdateQuantityEnum(item.getFoodId(), item.GetQuantityEnum().Previous());
		}
	}
}
