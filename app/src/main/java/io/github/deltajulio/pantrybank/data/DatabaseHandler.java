package io.github.deltajulio.pantrybank.data;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for dealing with the Firebase database
 */
public class DatabaseHandler
{
    private static final String TAG = "DatabaseHandler";

    // db name constants
    public static final String USER_PATH = "users";
    public static final String QUANTITY_TYPE = "quantityType";
    public static final String QUANTITY_NUM = "quantityNum";
    public static final String QUANTITY_APPROX = "quantityApprox";
    public static final String IS_PINNED = "isPinned";
    public static final String ITEMS = "items";
    public static final String CATEGORIES = "categories";

    private DatabaseReference databaseReference;
    private final String userId;

    public DatabaseHandler(String userId)
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
        String foodId = databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .push().getKey();

        Map<String, FoodItem> item = new HashMap<String, FoodItem>();
        foodItem.setFoodId(foodId);
        item.put(foodId, foodItem);

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

    public void UpdateIsPinned(String foodId, boolean isPinned)
    {
        databaseReference.child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodId)
                .child(IS_PINNED).setValue(isPinned);
    }

    public DatabaseReference GetDatabaseReference() { return databaseReference; }
}
