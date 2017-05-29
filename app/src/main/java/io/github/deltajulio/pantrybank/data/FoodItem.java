package io.github.deltajulio.pantrybank.data;

import java.io.Serializable;

/**
 * TODO: add a class header comment
 */

public class FoodItem implements Serializable
{
	private static final String TAG = FoodItem.class.getSimpleName();

	// db name constants
	public static final String NAME = "name";
	public static final String QUANTITY = "quantity";
	public static final String FOOD_ID = "foodId";
	public static final String IS_PINNED = "isPinned";
	public static final String CATEGORY_ID = "categoryId";

	private String name;
	private boolean isPinned;
	private String quantityType;
	private String quantity;
	private String categoryId;
	private String foodId;

	@SuppressWarnings("unused")
	public FoodItem() { /*Needed for Firebase*/ }

	public FoodItem(String name, boolean isPinned, long quantity,
			String categoryId)
	{
		setName(name);
		setIsPinned(isPinned);
		setQuantity(String.valueOf(quantity));
		setCategoryId(categoryId);
	}

	public final String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public final boolean getIsPinned() { return isPinned; }
	public void setIsPinned(boolean isPinned) { this.isPinned = isPinned; }

	/**
	 * For retrieving database value. Use long return value instead.
	 */
	public final String getQuantity() { return quantity; }

	/**
	 * For setting database value. Use long param version instead.
	 */
	public void setQuantity(String quantity) { this.quantity = quantity; }

	public final long GetQuantity() { return Long.parseLong(quantity);}
	public void SetQuantity(long quantity) { this.quantity = String.valueOf(quantity); }

	public String getCategoryId() { return categoryId; }
	public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

	public String getFoodId() { return foodId; }
	public void setFoodId(String foodId) { this.foodId = foodId; }
}
