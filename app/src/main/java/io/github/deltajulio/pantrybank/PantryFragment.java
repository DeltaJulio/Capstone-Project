package io.github.deltajulio.pantrybank;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import io.github.deltajulio.pantrybank.data.Category;
import io.github.deltajulio.pantrybank.data.DatabaseHandler;
import io.github.deltajulio.pantrybank.data.FoodItem;
import io.github.deltajulio.pantrybank.ui.MainFragmentListener;
import io.github.deltajulio.pantrybank.ui.adapter.PantryRecyclerAdapter;
import io.github.deltajulio.pantrybank.ui.adapter.BaseRecyclerAdapter;

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
    RecyclerView recyclerView;
    BaseRecyclerAdapter recyclerAdapter;
    private MainFragmentListener listener;

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
        DatabaseHandler databaseHandler = listener.GetDatabase();

        Query itemsRef = reference.child(DatabaseHandler.USER_PATH)
                .child(databaseHandler.GetUserId())
                .child(DatabaseHandler.ITEMS)
                .orderByChild(FoodItem.CATEGORY_ID);

        Query categoriesRef = reference.child(DatabaseHandler.USER_PATH)
                .child(databaseHandler.GetUserId())
                .child(DatabaseHandler.CATEGORIES)
                .orderByChild(Category.NAME);

        // Set up the RecyclerView
        recyclerView = (RecyclerView) view;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerAdapter = new PantryRecyclerAdapter(categoriesRef, itemsRef, listener, getContext());
        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        // Tells the recycler adapter to unregister its data listeners.
        recyclerAdapter.RemoveListeners();
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
