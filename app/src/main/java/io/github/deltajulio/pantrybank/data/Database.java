package io.github.deltajulio.pantrybank.data;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Utility class for dealing with the Firebase database
 */
public class Database
{
    // db name constants
    private static final String USER_PATH = "users";
    private static final String QUANTITY_TYPE = "quantityType";
    private static final String QUANTITY_NUM = "quantityNum";
    private static final String QUANTITY_APPROX = "quantityApprox";
    private static final String IS_PINNED = "isPinned";

    private DatabaseReference databaseReference;
    private String userId;

    public Database(String userId)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        this.userId = userId;
    }

    public void AddItem(FoodItem foodItem)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .push().setValue(foodItem);
    }

    public void DeleteItem(String foodName)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(foodName).removeValue();
    }

    public void UpdateQuantityType(String foodName, FoodItem.QuantityType quantityType)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(foodName)
                .child(QUANTITY_TYPE).setValue(quantityType);
    }

    public void UpdateQuantityNum(String foodName, long quantityNum)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(foodName)
                .child(QUANTITY_NUM).setValue(quantityNum);
    }

    public void UpdateQuantityApprox(String foodName, FoodItem.QuantityApprox quantityApprox)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(foodName)
                .child(QUANTITY_APPROX).setValue(quantityApprox);
    }

    public void UpdateIsPinned(String foodName, boolean isPinned)
    {
        databaseReference.child(USER_PATH)
                .child(userId)
                .child(foodName)
                .child(IS_PINNED).setValue(isPinned);
    }
}
