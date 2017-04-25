package io.github.deltajulio.pantrybank;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Bryan on 23-Apr-17.
 */

public class TabAdapter extends FragmentPagerAdapter
{
    final static int PAGE_COUNT = 3;
    private Context context;

    public TabAdapter(FragmentManager fragmentManager, Context context)
    {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0: return PantryFragment.newInstance();
            //case 1: return GroceryFragment.newInstance();
            //case 2: return RecipeFragment.newInstance();
            default: return PantryFragment.newInstance();
        }
    }
}
