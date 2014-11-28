package com.vernonsung.hellowordintentservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	// UI
	private Button buttonStartService;
	private Button buttonKillService;
	private Button buttonGetUuid;
	private Button buttonInsertRandom;
	
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
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get UI components
        buttonStartService = (Button)findViewById(R.id.buttonStartService);
        buttonKillService = (Button)findViewById(R.id.buttonKillService);
        buttonGetUuid = (Button)findViewById(R.id.buttonGetUuid);
        buttonInsertRandom = (Button)findViewById(R.id.buttonInsertRamdom);
        
        // Set listener
        buttonStartService.setOnClickListener(new Button.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		startPeopleIntentService();
        	}
        });
        buttonKillService.setOnClickListener(new Button.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		killPeopleIntentService();
        	}
        });
        buttonGetUuid.setOnClickListener(new Button.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		getUuid();
        	}
        });
        buttonInsertRandom.setOnClickListener(new Button.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		insertRandom();
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
    
    private void bindPeopleIntentService() {
    	Intent intent = new Intent(this, PeopleIntentService.class);
    	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unBindPeopleIntentService() {
    	if (mService != null) {
    		unbindService(mConnection);
    	}
    }
    
    private void getUuid() {
    	long uuid;
    	if (mService != null) {
	    	uuid = mService.getUuid();
	    	Toast.makeText(this, String.valueOf(uuid), Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(this, "Service is not bound", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void insertRandom() {
    	long id = mService.insertRandom();
    	Toast.makeText(this, "Row " + String.valueOf(id), Toast.LENGTH_SHORT).show();
    }
    
    private void killPeopleIntentService() {
    	if (mService != null)
    		mService.setToExit(true);
    }
}
