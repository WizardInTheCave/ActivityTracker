
package com.example.jack.coursework4_activitytracker;

/**
 * Created by Jack on 11/01/2017.
 */

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import java.util.ArrayList;

/**
 * This activity will allow the user to select a previous journey they have made.
 * So they can view it again.
 */
public class LoadJourneyActivity extends ListActivity {

    SimpleCursorAdapter dataAdapter;

    public static final String SELECTED_JOURNEY_TITLE = "SelectedJourneyTitle";

    /**
     * Get the contents of our list displaying all the different journeys done in the past
     * @return
     */
    private ArrayList<String> getJourneys(){

        ArrayList<String> journeys = new ArrayList<>();

        final String[] projection = {LocationsContentProviderContract.JOURNEY_NAMES_FIELD};

        Cursor cursor = getContentResolver().query(LocationsContentProviderContract.JOURNEY_NAMES_QUERY_URI,
                projection,
                null, null, null, null);

        // all the different past journeys that can be selected from
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String journeyName = cursor.getString(cursor.getColumnIndex(LocationsContentProviderContract.JOURNEY_NAMES_FIELD));
                    journeys.add(journeyName);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return journeys;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> journeys = getJourneys();
        setListAdapter(new ArrayAdapter<>(this, R.layout.activity_load_journey, journeys));
        final ListView listView = getListView();
        listView.setTextFilterEnabled(true);

        // set up list view so user can click on a journey that they want
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> myAdapter, View myView,
                                    int myItemInt, long mylng) {

                CharSequence txtView = ((TextView) myView).getText();
                String selectedJourneyTitle = txtView.toString();

                Toast.makeText(getApplicationContext(), txtView, Toast.LENGTH_SHORT).show();

                Intent result = new Intent();
                result.putExtra(SELECTED_JOURNEY_TITLE, selectedJourneyTitle);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}


