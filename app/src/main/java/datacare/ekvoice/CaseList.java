package datacare.ekvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import datacare.ekvoice.util.JSONToCase;
import java.util.ArrayList;
/**
 * created by Robin
 * This activity holds the list of cases pulled from the JSON and displays them to the user.
 */
public class CaseList extends Activity {
    private ListView listView;
    private ArrayList<Case> caseNames;
    CaseAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_list);


        try{
            caseNames = JSONToCase.readJsonStream(null);
        } catch (java.io.IOException e){

        }
        itemsAdapter = new CaseAdapter(this, caseNames);
        listView = (ListView) findViewById(R.id.listV);
        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Case toExpand = itemsAdapter.getItem(position);

                Intent expandEvent = new Intent(CaseList.this, CaseDetailsAdapter.class);
                expandEvent.putExtra("caseToExpand", toExpand);
                startActivity(expandEvent);
            }
        });
    }

    public void logoutButton(View v){
        finish();
        //startActivity(new Intent(this, LoginActivity.class));
    }

}