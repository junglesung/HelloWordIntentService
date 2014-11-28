package com.vernonsung.hellowordintentservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class PeopleIntentService extends IntentService {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mIntentFilter;
    private P2pNsdHelper mP2pNsdHelper;
    private WifiP2pReceiver mReceiver;
    private int wpsSetupType = WpsInfo.PBC;
    private HashMap<Long, Calendar> peopleList = new HashMap<Long, Calendar>();
	private boolean toExit = true;
	private long uuid;
//	private UUID uuid = UUID.randomUUID();
	private FriendDB mFriendDB = new FriendDB(this);

    // Constant
    private static final String LOG_TAG = "Debug";
	private static final int INTERVAL = 5000;  // 5 secs
	private static final int TOGETHER_MINUTE = 5;  // 5 minutes
	private static final int APART_MINUTE = 10;  // 10 minutes
    private final IBinder mBinder = new LocalBinder();  // Binder given to clients

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PeopleIntentService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PeopleIntentService.this;
        }
    }

    public PeopleIntentService()
    {
        super("PeopleIntentService");
    	uuid = new Random().nextLong();
    	if (uuid < 0) {
    		uuid = -uuid;
    	}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	// Ready
        initialWifiP2p();
        registerReceiver(mReceiver, mIntentFilter);
        startP2pPeerDiscovery();
        
        // Action
		toExit = false;
		while (!toExit) {
			Log.i(LOG_TAG, String.valueOf(new Random().nextInt(10)));
			synchronized (this) {
				try {
					wait(INTERVAL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// Clean up
        stopP2pPeerDiscovery();
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	public boolean isToExit() {
		return toExit;
	}

	public void setToExit(boolean toExit) {
		this.toExit = toExit;
	}

    private WifiP2pManager.DnsSdServiceResponseListener mDnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {

        @Override
        public void onDnsSdServiceAvailable(String instanceName,
                                            String registrationType, WifiP2pDevice srcDevice) {
            long hisUuid = Long.parseLong(instanceName);
            String message = "instanceName: " + instanceName +
                    ", registrationType: " + registrationType +
                    ", srcDevice.deviceAddress: " + srcDevice.deviceAddress +
                    ", srcDevice.deviceName: " + srcDevice.deviceName +
                    ", srcDevice.primaryDeviceType: " + srcDevice.primaryDeviceType +
                    ", srcDevice.secondaryDeviceType: " + srcDevice.secondaryDeviceType;
            switch (srcDevice.status) {
                case WifiP2pDevice.CONNECTED:
                    message += ", srcDevice.status: CONNECTED";
                    break;
                case WifiP2pDevice.AVAILABLE:
                    message += ", srcDevice.status: AVAILABLE";
                    break;
                case WifiP2pDevice.FAILED:
                    message += ", srcDevice.status: FAILED";
                    break;
                case WifiP2pDevice.INVITED:
                    message += ", srcDevice.status: INVITED";
                    break;
                case WifiP2pDevice.UNAVAILABLE:
                    message += ", srcDevice.status: UNAVAILABLE";
                    break;
                default:
                    break;
            }
            if (srcDevice.isGroupOwner())
                message += ", srcDevice is group owner";
            else
                message += ", srcDevice is not group owner";

            // Change UI
//			Log.d(this.getClass().getName(), new Date().toString() + " [onDnsSdServiceAvailable]" + message);
            Log.d(LOG_TAG, new Date().toString() + " [onDnsSdServiceAvailable] " + instanceName);
            
            // Request for TXT data
            WifiP2pDnsSdServiceRequest request = WifiP2pDnsSdServiceRequest.newInstance(instanceName, P2pNsdHelper.SERVICE_TYPE);
            mManager.addServiceRequest(mChannel, request, new WifiP2pManager.ActionListener() {
            	@Override
            	public void onSuccess() {
            		Log.d(LOG_TAG, new Date().toString() + " [AddTxtRequestOnSuccess] " + getString(R.string.wifi_p2p_ok));
            	}
            	@Override
            	public void onFailure(int reason) {
            		String message = getActionFailReason(reason);
            		Log.d(LOG_TAG, new Date().toString() + " [AddTxtRequestOnFailure] " + message);
            	}
            });

            // Action
            onReceiveHello(hisUuid);
        }

    };

    private WifiP2pManager.DnsSdTxtRecordListener mDnsSdTxtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> record, WifiP2pDevice srcDevice) {
            long hisUuid = Long.parseLong(fullDomainName);
            String message = "fullDomainName: " + fullDomainName +
                    ", srcDevice.deviceAddress: " + srcDevice.deviceAddress +
                    ", srcDevice.deviceName: " + srcDevice.deviceName +
                    ", srcDevice.primaryDeviceType: " + srcDevice.primaryDeviceType +
                    ", srcDevice.secondaryDeviceType: " + srcDevice.secondaryDeviceType;
            switch (srcDevice.status) {
                case WifiP2pDevice.CONNECTED:
                    message += ", srcDevice.status: CONNECTED";
                    break;
                case WifiP2pDevice.AVAILABLE:
                    message += ", srcDevice.status: AVAILABLE";
                    break;
                case WifiP2pDevice.FAILED:
                    message += ", srcDevice.status: FAILED";
                    break;
                case WifiP2pDevice.INVITED:
                    message += ", srcDevice.status: INVITED";
                    break;
                case WifiP2pDevice.UNAVAILABLE:
                    message += ", srcDevice.status: UNAVAILABLE";
                    break;
                default:
                    break;
            }
            if (srcDevice.isGroupOwner())
                message += ", srcDevice is group owner";
            else
                message += ", srcDevice is not group owner";
            // Print TXT data
            message += ", TXT: {";
            for (Entry<String, String> entry : record.entrySet()) {
                message += ", <" + entry.getKey() + ": " + entry.getValue() + ">";
            }
            message += "}";
            // Debug: print room list
            message += ", Room:{";
            for (Entry<Long, Calendar> entry : peopleList.entrySet()) {
                message += ", <" + entry.getKey() + ": " + entry.getValue() + ">";
            }
            message += "}";

            // Change UI
//			Log.d(LOG_TAG, new Date().toString() + " [onDnsSdTxtRecordAvailable]" + message);
            Log.d(LOG_TAG, new Date().toString() + " [onDnsSdTxtRecordAvailable]" + fullDomainName);
        }
    };

    private void initialWifiP2p() {
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mManager.setDnsSdResponseListeners(mChannel, mDnsSdServiceResponseListener, mDnsSdTxtRecordListener);

        mReceiver = new WifiP2pReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        
        mP2pNsdHelper = new P2pNsdHelper(this);
        mP2pNsdHelper.createRoom(uuid);
    }

    public void startP2pPeerDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, new Date().toString() + " [DiscoverPeersOnSuccess] " + getString(R.string.wifi_p2p_ok));
            }

            @Override
            public void onFailure(int reason) {
                String message = getActionFailReason(reason);
                Log.d(LOG_TAG, new Date().toString() + " [DiscoverPeersOnSuccess] " + message);
            }
        });
    }

    public void stopP2pPeerDiscovery() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, new Date().toString() + " [StopDiscoverPeersOnSuccess] " + getString(R.string.wifi_p2p_ok));
            }

            @Override
            public void onFailure(int reason) {
                String message = getActionFailReason(reason);
                Log.d(LOG_TAG, new Date().toString() + " [StopDiscoverPeersOnSuccess] " + message);
            }
        });
    }
    
    private String getActionFailReason(int reason) {
        switch (reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return getString(R.string.wifi_p2p_unsupported);
            case WifiP2pManager.BUSY:
                return getString(R.string.wifi_p2p_busy);
            case WifiP2pManager.ERROR:
                return getString(R.string.wifi_p2p_error);
            case WifiP2pManager.NO_SERVICE_REQUESTS:
                return getString(R.string.wifi_p2p_no_service_request);
            default:
                return getString(R.string.this_is_a_bug);
        }
    }

    private void onReceiveHello(long _uuid) {
    	// Check together
        Calendar togetherTime = peopleList.get(_uuid);
        Calendar nowTime = Calendar.getInstance();
        boolean addOrUpdate = false;
        
        if (togetherTime == null) {
        	// Add new device to the list.
        	addOrUpdate = true;
        } else if (nowTime.after(togetherTime)) {
        	// Diff.min = (now.ms - togetherTime.ms) / 1000 / 60 + TOGETHER_MINUTE
        	long diffMinutes = (nowTime.getTimeInMillis() - togetherTime.getTimeInMillis()) / 6000 + TOGETHER_MINUTE;
        	// Update DB
        	if (mFriendDB.updateFriend(_uuid, diffMinutes)) {
	        	// Update together time in people list.
	        	addOrUpdate = true;
        	}
        }
        // Add new device to the list or Update together time in people list.
        if (addOrUpdate) {
        	togetherTime = nowTime;
        	togetherTime.add(Calendar.MINUTE, TOGETHER_MINUTE);
        	peopleList.put(_uuid, togetherTime);
        }
    }
    
    private void cleanForgetful() {
    	//
    }

    public long getUuid() {
    	return uuid;
    }
    
    public long insertRandom() {
    	return mFriendDB.executeSample();
    }
}
