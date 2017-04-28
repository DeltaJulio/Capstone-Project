package io.github.deltajulio.pantrybank.data;

/**
 * TODO: add a class header comment
 */

public class FoodItem
{
    private final String TAG = "FoodItem";

    public enum QuantityType
    {
        NULL, NUMERICAL, APPROXIMATE
    }
    public enum QuantityApprox
    {
        NULL, NONE_REMAINING, RUNNING_LOW, NORMAL, PLENTY
    }

    private String name;
    private boolean isPinned;
    private String quantityType;
    private String quantity;
    private String categoryId;
    private String foodId;

    @SuppressWarnings("unused")
    public FoodItem() { /*Needed for Firebase ui*/ }

    private FoodItem(String name, boolean isPinned, QuantityType quantityType, String quantity,
                     String categoryId)
    {
        setName(name);
        setIsPinned(isPinned);
        setQuantityType(quantityType);
        setQuantity(quantity);
        setCategoryId(categoryId);
    }

    public FoodItem(String name, boolean isPinned, long quantityNum, String categoryId)
    {
        this(name, isPinned, QuantityType.NUMERICAL, String.valueOf(quantityNum), categoryId);
    }

    public FoodItem(String name, boolean isPinned, QuantityApprox quantityApprox, String categoryId)
    {
        this(name, isPinned, QuantityType.APPROXIMATE, quantityApprox.toString(), categoryId);
    }

    public FoodItem(String name, boolean isPinned, QuantityType quantityType, String categoryId)
    {
        this(name, isPinned, quantityType,
                (quantityType == QuantityType.NUMERICAL ?
                String.valueOf(1): QuantityApprox.NORMAL.toString()),
                categoryId);
    }

    public final String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public final boolean getIsPinned() { return isPinned; }
    public void setIsPinned(boolean isPinned) { this.isPinned = isPinned; }

    public final QuantityType getQuantityType() { return QuantityType.valueOf(quantityType); }
    public void setQuantityType(QuantityType quantityType) { this.quantityType = quantityType.toString(); }

    /**
     * For retrieving database value ONLY. Use long or enum return value instead.
     */
    public final String getQuantity() { return quantity; }

    /**
     * For setting database value ONLY. Use long or enum param version instead.
     */
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public final long GetQuantityLong() { return Long.parseLong(quantity);}
    public void SetQuantityLong(long quantity) { this.quantity = String.valueOf(quantity); }

    public final QuantityApprox GetQuantityEnum() { return QuantityApprox.valueOf(quantity); }
    public void SetQuantityEnum(QuantityApprox quantity) { this.quantity = quantity.toString(); }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
}
