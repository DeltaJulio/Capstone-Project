package io.github.deltajulio.pantrybank.data;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.github.deltajulio.pantrybank.R;

/**
 * Utility class for dealing with the Firebase database
 */
public class DatabaseHandler
{
    private static final String TAG = "DatabaseHandler";

    // db name constants
    public static final String USER_PATH = "users";
    public static final String ITEMS = "items";
    public static final String CATEGORIES = "categories";

    private DatabaseReference databaseReference;
    private final String userId;
    private static String uncategorizedCategoryId;

    public DatabaseHandler(String userId)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        this.userId = userId;
    }

    public static void SetUncategorizedCategoryId(String categoryId)
    {
	    uncategorizedCategoryId = categoryId;
    }

    public String GetUserId()
    {
        return userId;
    }

    public DatabaseReference GetFoodItems()
    {
	    return databaseReference.child(USER_PATH)
			    .child(userId)
			    .child(ITEMS);
    }

    public DatabaseReference GetCategories()
    {
	    return databaseReference.child(USER_PATH)
			    .child(userId)
			    .child(CATEGORIES);
    }

    public void AddCategory(Category category)
    {
	    String categoryId = GetCategories().push().getKey();

	    category.setCategoryId(categoryId);

      GetCategories()
		      .child(categoryId).setValue(category);
    }

    public void DeleteCategory(final String categoryId, final View parent)
    {
	    // Relocate all items under this category to "uncategorized"
	    // Get all items with this category
	    GetFoodItems().addListenerForSingleValueEvent(new ValueEventListener()
	    {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot)
		    {
			    for (DataSnapshot item : dataSnapshot.getChildren())
			    {
				    FoodItem food = item.getValue(FoodItem.class);
				    if (food.getCategoryId().equals(categoryId))
				    {
					    if (uncategorizedCategoryId == null || uncategorizedCategoryId.equalsIgnoreCase(""))
					    {
						    Log.e(TAG, "uncategorizedCategoryId is NULL");
						    throw null;
					    }

					    // FoodItem is in matching category, move to uncategorized
					    UpdateCategory(food.getFoodId(), uncategorizedCategoryId);
				    }
			    }

			    // Delete category from db
			    GetCategories()
					    .child(categoryId).removeValue();
		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError)
		    {
			    // Could not complete item migration, abort deletion.
			    Log.e(TAG, databaseError.toString());
			    Snackbar.make(parent, R.string.error_cannot_delete_category, Snackbar.LENGTH_LONG);
		    }
	    });
    }

    public void UpdateCategoryName(Category category)
    {
	    GetCategories().child(category.getCategoryId())
			    .setValue(category);
    }

    public void AddItem(FoodItem foodItem)
    {
        String foodId = databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .push().getKey();
        
        foodItem.setFoodId(foodId);

        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodId).setValue(foodItem);
    }

    public void DeleteItem(String foodId)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodId).removeValue();
    }

	public void UpdateName(String foodId, String name)
	{
		databaseReference
				.child(USER_PATH)
				.child(userId)
				.child(ITEMS)
				.child(foodId)
				.child(FoodItem.NAME).setValue(name);
	}

    private void UpdateQuantity(String foodId, String quantity)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodId)
                .child(FoodItem.QUANTITY).setValue(quantity);
    }

    public void UpdateQuantity(String foodId, long quantity)
    {
        UpdateQuantity(foodId, String.valueOf(quantity));
    }

    public void UpdateIsPinned(String foodId, boolean isPinned)
    {
        databaseReference
                .child(USER_PATH)
                .child(userId)
                .child(ITEMS)
                .child(foodId)
                .child(FoodItem.IS_PINNED).setValue(isPinned);
    }

	/**
	 * Updates the category associated with the provided foodId
	 */
	public void UpdateCategory(String foodId, String categoryId)
	{
		databaseReference
				.child(USER_PATH)
				.child(userId)
				.child(ITEMS)
				.child(foodId)
				.child(FoodItem.CATEGORY_ID).setValue(categoryId);
	}

    public DatabaseReference GetDatabaseReference() { return databaseReference; }
}
