package com.vernonsung.hellowordintentservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.vernonsung.hellowordintentservice.FriendDB.FriendPeople;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	// UI
	private TextView textViewUuid;
	private Button buttonRealTime;
	private Button buttonHistory;
	private ListView listViewFriend;
	
	// Variable
	private PeopleIntentService mService;
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PeopleIntentService.LocalBinder binder = (PeopleIntentService.LocalBinder)service;
			mService = binder.getService();
			initialDataFromPeopleIntentService();
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get UI components
        textViewUuid = (TextView)findViewById(R.id.textViewUuid);
        buttonRealTime = (Button)findViewById(R.id.buttonRealTime);
        buttonHistory = (Button)findViewById(R.id.buttonHistory);
        listViewFriend = (ListView)findViewById(R.id.listViewFriend);
        
        // Set listener
        buttonRealTime.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		updateRealTimeView();
        	}
        });
        buttonHistory.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		updateHistory();
        	}
        });
        
        // Action
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	protected void onStart() {
		super.onStart();
		bindPeopleIntentService();
		startPeopleIntentService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unBindPeopleIntentService();
	}

	private void bindPeopleIntentService() {
		Intent intent = new Intent(this, PeopleIntentService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void unBindPeopleIntentService() {
		if (mService != null) {
			unbindService(mConnection);
		}
	}

	private void startPeopleIntentService() {
    	Intent intent;
    	ComponentName c;

    	// PeopleIntentService is already running
    	if (mService != null && !mService.isToExit()) {
    		Toast.makeText(this, "PeopleIntentService is already running", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	intent = new Intent(this, PeopleIntentService.class);
    	try {
    		c = startService(intent);
    		if (c == null) {
    			Toast.makeText(this, "Return null", Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(this, c.toString(), Toast.LENGTH_SHORT).show();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	
	private void initialDataFromPeopleIntentService() {
		if (mService == null)
			return;
		textViewUuid.setText(String.valueOf(mService.getUuid()));
	}
    
    private void updateRealTimeView() {
    	// Debug
    	insertRandom();
    	
		if (mService == null) {
			Toast.makeText(MainActivity.this, R.string.service_is_not_ready, Toast.LENGTH_SHORT).show();
			return;
		}
		final String UUID = "UUID";
		final String TIME = "TIME";
		Hashtable<Long, Calendar> people = mService.getPeopleList();
		ArrayList<HashMap<String, Long>> list = new ArrayList<HashMap<String, Long>>(people.size());
		for (Entry<Long, Calendar> entry : people.entrySet()) {
			HashMap<String, Long> item = new HashMap<String, Long>(2);
			item.put(UUID, entry.getKey());
			item.put(TIME, entry.getValue().getTimeInMillis());
			list.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(
				this, list, android.R.layout.simple_list_item_2, 
				new String [] {UUID, TIME}, 
				new int [] {android.R.id.text1, android.R.id.text2} );
		listViewFriend.setAdapter(adapter);
    }
    
    private void updateHistory() {
    	// Debug
    	insertRandomDb();

		Cursor c = mService.getFriends();
		if (c == null) {
			Toast.makeText(this, R.string.read_database_failed, Toast.LENGTH_SHORT).show();
			return;
		}
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, android.R.layout.simple_list_item_2, c, 
				new String[] {FriendPeople.COLUMN_NAME_UUID, FriendPeople.COLUMN_NAME_TIME}, 
				new int [] {android.R.id.text1, android.R.id.text2}, 0);
		listViewFriend.setAdapter(adapter);
    }
    
    // Debug
    private void insertRandom() {
    	int num = mService.insertRandom();
    	Toast.makeText(this, String.valueOf(num) + " rows", Toast.LENGTH_SHORT).show();
    }
    
    // Debug
    private void insertRandomDb() {
    	int num = mService.insertRandomDb();
    	Toast.makeText(this, String.valueOf(num) + " rows", Toast.LENGTH_SHORT).show();
    }
    
    // Debug
    private void killPeopleIntentService() {
    	if (mService != null)
    		mService.setToExit(true);
    }
}
