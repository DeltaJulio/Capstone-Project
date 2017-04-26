package io.github.deltajulio.pantrybank.ui;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import io.github.deltajulio.pantrybank.data.Database;
import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * The Firebase UI version of RecyclerAdapter. Also handles on click methods for items.
 */
public class PantryRecyclerAdapter extends FirebaseRecyclerAdapter<FoodItem, PantryFoodHolder> implements PantryOnClickListener
{
    private final Database database;

    public PantryRecyclerAdapter(Class<FoodItem> modelClass, int modelLayout,
                                 Class<PantryFoodHolder> viewHolderClass, Query ref,
                                 MainFragmentListener onClickListener)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        database = onClickListener.GetDatabase();
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
        viewHolder.SetItemName(item.GetName());
        viewHolder.SetIsPinned(item.GetIsPinned());

        if (item.GetQuantityType() == FoodItem.QuantityType.NUMERICAL)
        {
            viewHolder.SetItemQuantity(String.valueOf(item.GetQuantityNum()));
        } else
        {
            viewHolder.SetItemQuantity(item.GetQuantityApprox().toString());
        }
    }

    @Override
    public void OnPinClicked(final int position)
    {
        FoodItem item = getItem(position);
        database.UpdateIsPinned(item.GetName(), !item.GetIsPinned());
    }

    @Override
    public void OnDropDownClicked(final int position, PantryFoodHolder holder)
    {
        ToggleButton dropDown = holder.GetDropDown();
        holder.SetDropDownChecked(!dropDown.isChecked());
        holder.SetActionListVisibility(dropDown.isChecked() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void OnEditClicked(final int position)
    {

    }
}
