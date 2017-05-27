package io.github.deltajulio.pantrybank.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * TODO: add a class header comment
 */

public class CategorizedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final String TAG = CategorizedRecyclerAdapter.class.getSimpleName();
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
		return (AdapterUtils.IsCategorizedPosition(categories, position)
				? Integer.MAX_VALUE - categories.indexOfKey(position)
				: baseAdapter.getItemId(AdapterUtils.CategorizedPositionToPosition(categories, position)));
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
		if (AdapterUtils.IsCategorizedPosition(categories, position))
		{
			((CategoryHolder)viewHolder).title.setText(categories.get(position).GetTitle());
		} else
		{
			baseAdapter.onBindViewHolder(viewHolder,
					AdapterUtils.CategorizedPositionToPosition(categories, position));
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		if (AdapterUtils.IsCategorizedPosition(categories, position))
		{
			return SECTION_TYPE;
		} else
		{
			return baseAdapter.getItemViewType
					(AdapterUtils.CategorizedPositionToPosition(categories, position) + 1);
		}
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

	public SparseArray<Category> GetCategories()
	{
		return categories;
	}
}
