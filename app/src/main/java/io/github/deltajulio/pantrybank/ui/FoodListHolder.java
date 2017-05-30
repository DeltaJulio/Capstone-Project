package io.github.deltajulio.pantrybank.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import io.github.deltajulio.pantrybank.R;
import io.github.deltajulio.pantrybank.ui.adapter.FoodListAdapter;

public class FoodListHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
	private static final String TAG = FoodListHolder.class.getSimpleName();

	private TextView itemName;
	private ImageButton buttonDelete;

	private FoodListAdapter adapter;

	public FoodListHolder(FoodListAdapter adapter, View itemView)
	{
		super(itemView);
		this.adapter = adapter;

		itemName = (TextView) itemView.findViewById(R.id.item_text);
		buttonDelete = (ImageButton) itemView.findViewById(R.id.button_delete);

		buttonDelete.setOnClickListener(this);
	}

	public void SetItemName(String name) { itemName.setText(name); }

	@Override
	public void onClick(View v)
	{
		if (v.getId() == buttonDelete.getId())
		{
			adapter.OnDeleteClicked(getAdapterPosition());
		}
	}
}
