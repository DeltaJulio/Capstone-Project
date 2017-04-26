package io.github.deltajulio.pantrybank.ui;

import io.github.deltajulio.pantrybank.data.Database;

/**
 * Allows classes down the chain to update database information
 */

public interface MainFragmentListener
{
    Database GetDatabase();
}
