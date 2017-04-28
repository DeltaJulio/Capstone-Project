package io.github.deltajulio.pantrybank.data;

import android.util.Log;

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
    private long quantityNum;
    private String quantityApprox;
    private String foodId;

    @SuppressWarnings("unused")
    public FoodItem() { /*Needed for Firebase ui*/ }

    public FoodItem(String name, boolean isPinned, QuantityType quantityType, long quantityNum, QuantityApprox quantityApprox)
    {
        this.name = name;
        this.isPinned = isPinned;
        this.quantityType = quantityType.toString();
        this.quantityNum = quantityNum;
        this.quantityApprox = quantityApprox.toString();
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public boolean getIsPinned() { return isPinned; }

    public void setIsPinned(boolean isPinned) { this.isPinned = isPinned; }

    public QuantityType getQuantityType() { return QuantityType.valueOf(quantityType); }

    public void setQuantityType(QuantityType quantityType) { this.quantityType = quantityType.toString(); }

    public long getQuantityNum() { return quantityNum; }

    public void setQuantityNum(long quantityNum) { this.quantityNum = quantityNum; }

    public QuantityApprox getQuantityApprox() { return QuantityApprox.valueOf(quantityApprox); }

    public void setQuantityApprox(QuantityApprox quantityApprox) { this.quantityApprox = quantityApprox.toString(); }

    public String getFoodId() { return foodId; }

    public void setFoodId(String foodId) { this.foodId = foodId; }
}
