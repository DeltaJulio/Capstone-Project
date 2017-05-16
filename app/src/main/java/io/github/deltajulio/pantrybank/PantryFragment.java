package io.github.deltajulio.pantrybank;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.CategorizedRecyclerAdapter;
import io.github.deltajulio.pantrybank.ui.PantryFoodHolder;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;
import io.github.deltajulio.pantrybank.ui.PantryRecyclerAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link MainFragmentListener}
 * interface.
 */
public class PantryFragment extends Fragment
{
    private static final String TAG = "PantryFragment";

    DatabaseReference reference;
    PantryRecyclerAdapter recyclerAdapter;
    private MainFragmentListener listener;

    RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PantryFragment()
    {
    }

    @SuppressWarnings("unused")
    public static PantryFragment newInstance()
    {
        return new PantryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        reference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);
        DatabaseHandler databaseHandlerHandler = listener.GetDatabase();

        // Set the adapter
        recyclerView = (RecyclerView) view;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerAdapter = new PantryRecyclerAdapter(FoodItem.class, R.layout.pantry_item,
                PantryFoodHolder.class,
                reference.child(DatabaseHandler.USER_PATH)
                        .child(databaseHandlerHandler.GetUserId())
                        .child(DatabaseHandler.ITEMS)
                , listener);

        // Categories adapter
        List<CategorizedRecyclerAdapter.Category> categories
            = new ArrayList<CategorizedRecyclerAdapter.Category>();

        // test Categories
        categories.add(new CategorizedRecyclerAdapter.Category(0, "Category 1"));
        //categories.add(new CategorizedRecyclerAdapter.Category(4, "Category 2"));

        // Add pantry item adapter to category adapter
        CategorizedRecyclerAdapter.Category[] dummy
            = new CategorizedRecyclerAdapter.Category[categories.size()];
        CategorizedRecyclerAdapter categorizedAdapter = new
            CategorizedRecyclerAdapter(getContext(), R.layout.category
            , R.id.category_text, recyclerAdapter);
        categorizedAdapter.SetCategories(categories.toArray(dummy));

        recyclerView.setAdapter(categorizedAdapter);

        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        recyclerAdapter.cleanup();
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof MainFragmentListener)
        {
            listener = (MainFragmentListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
