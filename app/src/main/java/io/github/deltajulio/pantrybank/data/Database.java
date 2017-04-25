package io.github.deltajulio.pantrybank.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Bryan on 25-Apr-17.
 */

public final class Database
{
    private Database() {}

    public static void AddItem()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("test");

        reference.setValue("this is a test");
    }
}
