package io.github.deltajulio.pantrybank.data;

/**
 * TODO: add a class header comment
 */

public class FoodItem
{
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
    private QuantityType quantityType;
    private long quantityNum;
    private QuantityApprox quantityApprox;

    @SuppressWarnings("unused")
    public FoodItem() { /*Needed for Firebase ui*/ }

    public FoodItem(String name, boolean isPinned, QuantityType quantityType, long quantityNum, QuantityApprox quantityApprox)
    {
        this.name = name;
        this.isPinned = isPinned;
        this.quantityType = quantityType;
        this.quantityNum = quantityNum;
        this.quantityApprox = quantityApprox;
    }

    public String GetName() { return name; }

    public void SetName(String name) { this.name = name; }

    public boolean GetIsPinned() { return isPinned; }

    public void SetIsPinned(boolean isPinned) { this.isPinned = isPinned; }

    public QuantityType GetQuantityType() { return quantityType; }

    public void SetQuantityType(QuantityType quantityType) { this.quantityType = quantityType; }

    public long GetQuantityNum() { return quantityNum; }

    public void SetQuantityNum(long quantityNum) { this.quantityNum = quantityNum; }

    public QuantityApprox GetQuantityApprox() { return quantityApprox; }

    public void SetQuantityApprox(QuantityApprox quantityApprox) { this.quantityApprox = quantityApprox; }
}
