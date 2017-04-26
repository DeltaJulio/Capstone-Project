package io.github.deltajulio.pantrybank.data;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for dealing with the Firebase database
 */
public class Database
{
    // db name constants
    public static final String USER_PATH = "users";
    public static final String QUANTITY_TYPE = "quantityType";
    public static final String QUANTITY_NUM = "quantityNum";
    public static final String QUANTITY_APPROX = "quantityApprox";
    public static final String IS_PINNED = "isPinned";
    public static final String ITEMS = "items";

    private DatabaseReference databaseReference;
    private final String userId;

    public Database(String userId)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        this.userId = userId;
    }

    public String GetUserId()
    {
        return userId;
    }

    public void AddItem(FoodItem foodItem)
    {
        Map<String, FoodItem> item = new HashMap<String, FoodItem>();
        item.put(foodItem.getName(), foodItem);

        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .setValue(item);
    }

    public void DeleteItem(String foodName)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodName).removeValue();
    }

    public void UpdateQuantityType(String foodName, FoodItem.QuantityType quantityType)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodName)
                .child(QUANTITY_TYPE).setValue(quantityType);
    }

    public void UpdateQuantityNum(String foodName, long quantityNum)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodName)
                .child(QUANTITY_NUM).setValue(quantityNum);
    }

    public void UpdateQuantityApprox(String foodName, FoodItem.QuantityApprox quantityApprox)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodName)
                .child(QUANTITY_APPROX).setValue(quantityApprox);
    }

    public void UpdateIsPinned(String foodName, boolean isPinned)
    {
        databaseReference.child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodName)
                .child(IS_PINNED).setValue(isPinned);
    }
}
