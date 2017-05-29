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
    private ImageButton buttonEdit;

    private PantryOnClickListener onClickListener;

    public PantryFoodHolder(View itemView)
    {
        super(itemView);

        itemName = (TextView) itemView.findViewById(R.id.item_name);
        itemQuantity = (TextView) itemView.findViewById(R.id.item_quantity);
        itemPin = (CheckBox) itemView.findViewById(R.id.item_pin);
        buttonEdit = (ImageButton) itemView.findViewById(R.id.button_edit);

        itemPin.setOnClickListener(this);
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

    /**
     * Sends a message to the recycler adapter, which handles logic and database work.
     */
    @Override
    public void onClick(View v)
    {
        if (v.getId() == itemPin.getId())
        {
            onClickListener.OnPinClicked(getAdapterPosition());
        } else if (v.getId() == buttonEdit.getId())
        {
            onClickListener.OnEditClicked(getAdapterPosition());
        }
    }
}
