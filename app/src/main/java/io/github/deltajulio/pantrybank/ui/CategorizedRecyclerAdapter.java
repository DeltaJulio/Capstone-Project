package io.github.deltajulio.pantrybank.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import java.util.Arrays;
import java.util.Comparator;

import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * TODO: add a class header comment
 */

public class CategorizedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final String TAG = "CategoryAdapter";
	private final Context context;
	private static final int SECTION_TYPE = 0;

	private boolean valid = true;
	private int categoryResourceId;
	private int textResourceId;
	private LayoutInflater layoutInflater;
	private RecyclerView.Adapter baseAdapter;
	private SparseArray<Category> categories = new SparseArray<Category>();

	public CategorizedRecyclerAdapter(Context context, int categoryResourceId, int textResourceId,
	                                  RecyclerView.Adapter baseAdapter)
	{
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.categoryResourceId = categoryResourceId;
		this.textResourceId = textResourceId;
		this.baseAdapter = baseAdapter;
		this.context = context;

		this.baseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
		{
			@Override
			public void onChanged()
			{
				valid = CategorizedRecyclerAdapter.this.baseAdapter.getItemCount() > 0;
				notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount)
			{
				valid = CategorizedRecyclerAdapter.this.baseAdapter.getItemCount() > 0;
				notifyItemRangeChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount)
			{
				valid = CategorizedRecyclerAdapter.this.baseAdapter.getItemCount() > 0;
				notifyItemRangeInserted(positionStart, itemCount);
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount)
			{
				valid = CategorizedRecyclerAdapter.this.baseAdapter.getItemCount() > 0;
				notifyItemRangeRemoved(positionStart, itemCount);
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return (valid ? baseAdapter.getItemCount() + categories.size() : 0);
	}

	@Override
	public long getItemId(int position)
	{
		return (IsCategorizedPosition(position)
		       ? Integer.MAX_VALUE - categories.indexOfKey(position)
		       : baseAdapter.getItemId(CategorizedPositionToPosition(position)));
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == SECTION_TYPE)
		{
			final View view = LayoutInflater.from(context).inflate(categoryResourceId, parent, false);
			return new CategoryHolder(view, textResourceId);
		} else
		{
			return baseAdapter.onCreateViewHolder(parent, viewType - 1);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position)
	{
		if (IsCategorizedPosition(position))
		{
			((CategoryHolder)viewHolder).title.setText(categories.get(position).GetTitle());
		} else
		{
			baseAdapter.onBindViewHolder(viewHolder, CategorizedPositionToPosition(position));

			// baseAdapter only takes ViewHolders of type PantryFoodHolder,
			// but this adapter only provides the type CategoryHolder
			//PantryFoodHolder pantryFoodHolder = new PantryFoodHolder(viewHolder.itemView);
			//baseAdapter.onBindViewHolder(pantryFoodHolder, CategorizedPositionToPosition(position));
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		return super.getItemViewType(position);
	}

	public static class CategoryHolder extends RecyclerView.ViewHolder
	{
		public final TextView title;

		public CategoryHolder(View view, int textResourceId)
		{
			super(view);
			title = (TextView) view.findViewById(textResourceId);
		}
	}

	public void SetCategories(Category[] categories)
	{
		this.categories.clear();

		Arrays.sort(categories, new Comparator<Category>()
		{
			@Override
			public int compare(Category o1, Category o2)
			{
				return (o1.firstPosition == o2.firstPosition)
				       ? 0
				       : ((o1.firstPosition < o2.firstPosition) ? -1 : 1);
			}
		});

		int offset = 0;
		for (Category category : categories)
		{
			category.categorizedPosition = category.firstPosition + offset;
			this.categories.append(category.categorizedPosition, category);
		}
	}

	public int PositionToCategorizedPosition(int position)
	{
		int offset = 0;
		for (int i = 0; i < categories.size(); i++)
		{
			if (categories.valueAt(i).firstPosition > position)
			{
				break;
			}
			++offset;
		}

		return position + offset;
	}

	public int CategorizedPositionToPosition(int categorizedPosition)
	{
		if (IsCategorizedPosition(categorizedPosition))
		{
			return RecyclerView.NO_POSITION;
		}

		int offset = 0;
		for (int i = 0; i < categories.size(); i++)
		{
			if (categories.valueAt(i).categorizedPosition > categorizedPosition)
			{
				break;
			}
			--offset;
		}

		return categorizedPosition + offset;
	}

	public boolean IsCategorizedPosition(int position)
	{
		return categories.get(position) != null;
	}

	public static class Category
	{
		int firstPosition;
		int categorizedPosition;
		CharSequence title;

		public Category(int firstPosition, CharSequence title)
		{
			this.firstPosition = firstPosition;
			this.title = title;
		}

		public CharSequence GetTitle() { return title; }
	}

}
