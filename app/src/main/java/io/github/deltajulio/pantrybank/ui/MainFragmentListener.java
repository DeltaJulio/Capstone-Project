package io.github.deltajulio.pantrybank.ui;

import io.github.deltajulio.pantrybank.data.DatabaseHandler;

/**
 * Allows classes down the chain to update database information
 */

public interface MainFragmentListener
{
    DatabaseHandler GetDatabase();
}
