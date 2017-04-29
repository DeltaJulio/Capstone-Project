package io.github.deltajulio.pantrybank.ui;

import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;

/**
 * Allows classes down the chain to update database information
 */

public interface MainFragmentListener
{
    DatabaseHandler GetDatabase();

    void LaunchEditItemActivity(FoodItem item);
}
