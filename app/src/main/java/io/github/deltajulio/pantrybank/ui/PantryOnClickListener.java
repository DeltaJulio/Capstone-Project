package io.github.deltajulio.pantrybank.ui;

/**
 * Informs the recycler adapter that certain buttons have been pressed within the list.
 */

public interface PantryOnClickListener
{
    void OnPinClicked(final int position);

    void OnDropDownClicked(final int position, PantryFoodHolder holder);

    void OnEditClicked(final int position);

    void OnAddClicked(final int position);

    void OnRemoveClicked(final int position);
}
