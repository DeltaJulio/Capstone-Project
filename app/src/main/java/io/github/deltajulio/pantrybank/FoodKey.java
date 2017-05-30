package io.github.deltajulio.pantrybank;

import android.support.annotation.NonNull;

/**
 * Used to sort FoodItems in a list, based on Category name and FoodItem name.
 */

public class FoodKey implements Comparable<FoodKey>
{
	public String categoryName;
	public String foodName;

	public FoodKey(String categoryName, String foodName)
	{
		this.categoryName = categoryName;
		this.foodName = foodName;
	}

	@Override
	public int compareTo(@NonNull FoodKey o)
	{
		int categoryCompare = categoryName.compareTo(o.categoryName);
		if (categoryCompare != 0)
		{
			return categoryCompare;
		} else
		{
			return foodName.compareTo(o.foodName);
		}
	}
}
