package io.github.deltajulio.pantrybank.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import io.github.deltajulio.pantrybank.R;

/**
 * TODO: add a class header comment
 */

public class PantryFoodHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private static final String TAG = "PantryFoodHolder";

    private TextView itemName;
    private TextView itemQuantity;
    private CheckBox itemPin;
    private ToggleButton dropDown;
    private ImageButton buttonEdit;
    private LinearLayout actionList;

    private PantryOnClickListener onClickListener;

    public PantryFoodHolder(View itemView)
    {
        super(itemView);

        itemName = (TextView) itemView.findViewById(R.id.item_name);
        itemQuantity = (TextView) itemView.findViewById(R.id.item_quantity);
        itemPin = (CheckBox) itemView.findViewById(R.id.item_pin);
        dropDown = (ToggleButton) itemView.findViewById(R.id.button_drop_down);
        buttonEdit = (ImageButton) itemView.findViewById(R.id.button_edit);
        actionList = (LinearLayout) itemView.findViewById(R.id.action_list);

        itemPin.setOnClickListener(this);
        dropDown.setOnClickListener(this);
        buttonEdit.setOnClickListener(this);
    }

    public void SetItemName(String name) { itemName.setText(name); }

    public void SetItemQuantity(String quantity) { itemQuantity.setText(quantity.toLowerCase()); }

    public void SetIsPinned(boolean isPinned) { itemPin.setChecked(isPinned); }

    public PantryOnClickListener GetOnClickListener() { return onClickListener; }

    public void SetOnClickListener(PantryOnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public final ToggleButton GetDropDown() { return dropDown; }

    public void SetDropDownChecked(boolean isChecked)
    {
        dropDown.setChecked(isChecked);
    }

    public void SetActionListVisibility(int visibility)
    {
        actionList.setVisibility(visibility);
    }

    /**
     * Sends a message to the recycler adapter, which handles logic and database work.
     */
    @Override
    public void onClick(View v)
    {
        if (v.getId() == itemPin.getId())
        {
            onClickListener.OnPinClicked(getAdapterPosition());
        } else if (v.getId() == dropDown.getId())
        {
            onClickListener.OnDropDownClicked(getAdapterPosition(), this);
        } else if (v.getId() == buttonEdit.getId())
        {
            onClickListener.OnEditClicked(getAdapterPosition());
        }
    }
}
