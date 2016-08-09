package com.kaist.safetydriving;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewHistoryActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        ListView lvHistory = (ListView)findViewById(R.id.lvHistrory);
        DatabaseHelper mDB = new DatabaseHelper(this);
        Cursor constantsCursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM history", null);
        ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.row, constantsCursor,
                new String[] { DatabaseHelper.CONTENT, DatabaseHelper.SENDER, DatabaseHelper.ARRIVALTIME },
                new int[] {R.id.content, R.id.sender, R.id.arrival_time});
        lvHistory.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
