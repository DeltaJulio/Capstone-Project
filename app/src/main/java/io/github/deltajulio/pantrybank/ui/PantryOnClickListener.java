package io.github.deltajulio.pantrybank.ui;

/**
 * Informs the recycler adapter that certain buttons have been pressed within the list.
 */

public interface PantryOnClickListener
{
    void OnPinClicked(final int position);

    void OnEditClicked(final int position);
}
