package io.github.deltajulio.pantrybank.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import io.github.deltajulio.pantrybank.MainActivity;
import io.github.deltajulio.pantrybank.NewItemActivity;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * The Firebase UI version of RecyclerAdapter. Also handles on click methods for items.
 */
public class PantryRecyclerAdapter extends FirebaseRecyclerAdapter<FoodItem, PantryFoodHolder> implements PantryOnClickListener
{
    private static final String TAG = "PantryRecycleAdapter";
    private final DatabaseHandler databaseHandler;
    private final MainFragmentListener mainListener;

    public PantryRecyclerAdapter(Class<FoodItem> modelClass, int modelLayout,
                                 Class<PantryFoodHolder> viewHolderClass, Query ref,
                                 MainFragmentListener onClickListener)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.mainListener = onClickListener;
        databaseHandler = onClickListener.GetDatabase();
    }

	@Override
	public PantryFoodHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new PantryFoodHolder(parent);
	}

    @Override
    public void onBindViewHolder(PantryFoodHolder viewHolder, int position)
    {
        super.onBindViewHolder(viewHolder, position);
        viewHolder.SetOnClickListener(this);
    }

    @Override
    protected void populateViewHolder(PantryFoodHolder viewHolder, FoodItem item, int position)
    {
        viewHolder.SetItemName(item.getName());
        viewHolder.SetIsPinned(item.getIsPinned());
        
        if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
        {
            viewHolder.SetItemQuantity(String.valueOf(item.GetQuantityLong()));
        } else
        {
            viewHolder.SetItemQuantity(item.GetQuantityEnum().toString());
        }
    }

    @Override
    public void OnPinClicked(final int position)
    {
        FoodItem item = getItem(position);
        databaseHandler.UpdateIsPinned(item.getFoodId(), !item.getIsPinned());
    }

    @Override
    public void OnDropDownClicked(final int position, PantryFoodHolder holder)
    {
        ToggleButton dropDown = holder.GetDropDown();
        holder.SetActionListVisibility(dropDown.isChecked() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void OnEditClicked(final int position)
    {
        // Launch new item activity in edit mode
        mainListener.LaunchEditItemActivity(getItem(position));
    }

    @Override
    public void OnAddClicked(final int position)
    {
        FoodItem item = getItem(position);
        if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
        {
            databaseHandler.UpdateQuantityLong(item.getFoodId(), item.GetQuantityLong() + 1);
        } else
        {
            databaseHandler.UpdateQuantityEnum(item.getFoodId(), item.GetQuantityEnum().Next());
        }
    }

    @Override
    public void OnRemoveClicked(final int position)
    {
        FoodItem item = getItem(position);
        if (item.getQuantityType() == FoodItem.QuantityType.NUMERICAL)
        {
            long quantity = item.GetQuantityLong() - 1;
            databaseHandler.UpdateQuantityLong(item.getFoodId(), (quantity >= 0 ? quantity : 0));
        } else
        {
            databaseHandler.UpdateQuantityEnum(item.getFoodId(), item.GetQuantityEnum().Previous());
        }
    }

	@Override
	public int getItemCount()
	{
		Log.d(TAG, "getItemCount: " + String.valueOf(super.getItemCount()));
		return super.getItemCount();
	}
}
