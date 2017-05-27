package io.github.deltajulio.pantrybank.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Common functions that our recycler adapters will use.
 */

public final class AdapterUtils
{
	private AdapterUtils() {}

	public static void SetCategories(SparseArray<CategorizedRecyclerAdapter.Category> categories,
	                                 CategorizedRecyclerAdapter.Category[] newCategories)
	{
		categories.clear();

		Arrays.sort(newCategories, new Comparator<CategorizedRecyclerAdapter.Category>()
		{
			@Override
			public int compare(CategorizedRecyclerAdapter.Category o1,
			                   CategorizedRecyclerAdapter.Category o2)
			{
				if (o1.firstPosition == o2.firstPosition)
				{
					return 0;
				} else
				{
					return (o1.firstPosition < o2.firstPosition) ? -1 : 1;
				}
			}
		});

		int offset = 0;
		for (CategorizedRecyclerAdapter.Category category : newCategories)
		{
			category.categorizedPosition = category.firstPosition + offset;
			categories.append(category.categorizedPosition, category);
		}
	}

	public static int PositionToCategorizedPosition
			(SparseArray<CategorizedRecyclerAdapter.Category> categories, int position)
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

	public static int CategorizedPositionToPosition
			(SparseArray<CategorizedRecyclerAdapter.Category> categories, int categorizedPosition)
	{
		if (IsCategorizedPosition(categories, categorizedPosition))
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

	public static boolean IsCategorizedPosition
			(SparseArray<CategorizedRecyclerAdapter.Category> categories, int position)
	{
		return categories.get(position) != null;
	}
}
