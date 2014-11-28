package com.vernonsung.hellowordintentservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

public class WifiP2pReceiver extends BroadcastReceiver {
	// Constant
	private static final String LOG_TAG = "Debug";

	// Variable
	private WifiP2pManager nManager;
    private WifiP2pManager.Channel nChannel;
    private Context nContext;

    private void printNetworkInfo(NetworkInfo networkInfo) {
        Date now = new Date();
        if (networkInfo == null) {
            Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_network_info_is_null));
            return;
        }
        if (networkInfo.isConnected()) {
            Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_connected));
            nManager.requestConnectionInfo(nChannel, nConnectionInfoListener);
        } else {
            // Get connection state
            switch (networkInfo.getState()) {
                case CONNECTED:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_connected));
                    break;
                case CONNECTING:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_connecting));
                    break;
                case DISCONNECTED:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_disconnected));
                    break;
                case DISCONNECTING:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_disconnecting));
                    break;
                case SUSPENDED:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_suspended));
                    break;
                default:
                    Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_is_unknown));
                    break;
            }
        }
    }
    private void printGroupInfo(WifiP2pGroup groupInfo) {
        String message;
        Date now = new Date();
        if (groupInfo == null) {
            Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_network_info_is_null));
            return;
        }
        message = "Interface: " + groupInfo.getInterface();
        message += ", NetworkName: " + groupInfo.getNetworkName();
        message += ", Passphrase: " + groupInfo.getPassphrase();
        ArrayList<WifiP2pDevice> clients = new ArrayList<WifiP2pDevice>(groupInfo.getClientList());
        message += ", clients:";
        for (WifiP2pDevice device : clients) {
            message += " <" + device.deviceName;
            message += ", " + device.deviceAddress;
            message += ", " + device.primaryDeviceType;
            message += ", " + device.secondaryDeviceType;
            switch (device.status) {
                case WifiP2pDevice.AVAILABLE:
                    message += ", available>";
                    break;
                case WifiP2pDevice.CONNECTED:
                    message += ", connected>";
                    break;
                case WifiP2pDevice.FAILED:
                    message += ", failed>";
                    break;
                case WifiP2pDevice.INVITED:
                    message += ", invited>";
                    break;
                case WifiP2pDevice.UNAVAILABLE:
                    message += ", unavailable>";
                    break;
                default:
                    message += ", unknown>";
                    break;
            }
        }
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + message);
    }

    private WifiP2pManager.ConnectionInfoListener nConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // Request group owner IP
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            String message = nContext.getString(R.string.group_owner_is) + " " + groupOwnerAddress.toString();

            if (info.groupFormed)
                message += ", " + nContext.getString(R.string.group_is_formed);
            else
                message += ", " + nContext.getString(R.string.group_is_not_formed);
            if (info.isGroupOwner)
                message += ", " + nContext.getString(R.string.i_am_group_owner);
            else
                message += ", " + nContext.getString(R.string.i_am_not_group_owner);
            Log.d(LOG_TAG, new Date().toString() + "[onConnectionInfoAvailable] " + message);
        }
    };

    private WifiP2pManager.GroupInfoListener nGroupInfoListener = new WifiP2pManager.GroupInfoListener() {

        @Override
        public void onGroupInfoAvailable(WifiP2pGroup groupInfo) {
            printGroupInfo(groupInfo);
        }
    };

    public WifiP2pReceiver(WifiP2pManager _manager,
                          WifiP2pManager.Channel _channel,
                          Context _context) {
        super();
        nManager = _manager;
        nChannel = _channel;
        nContext = _context;
    }

    private void wifiP2pStateChangedActionHandler(Intent intent) {
        // Log only
        String message;
        Date now = new Date();
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, 0);
        switch (state) {
            case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                message = nContext.getString(R.string.wifi_p2p_on);
                break;
            case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                message = nContext.getString(R.string.wifi_p2p_off);
                break;
            default:
                message = nContext.getString(R.string.this_is_a_bug);
                break;
        }
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_STATE_CHANGED_ACTION] " + message);
    }

    private void wifiP2pPeersChangedActionHandler(Intent intent) {
        // Log only
        String message;
        Date now = new Date();
        message = nContext.getString(R.string.wifi_p2p_peers_change);
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_PEERS_CHANGED_ACTION] " + message);
    }

    private void wifiP2pConnectionChangeActionHandler(Intent intent) {
        String message;
        Date now = new Date();
        message = nContext.getString(R.string.wifi_p2p_connection_change);
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + message);
        if (nManager == null) {
            Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_CONNECTION_CHANGED_ACTION] " + nContext.getString(R.string.wifi_p2p_manager_is_null));
            return;
        }
        // Get network info
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        printNetworkInfo(networkInfo);
        // Get group info
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // < 4.3 API 18
            nManager.requestGroupInfo(nChannel, nGroupInfoListener);
        } else {
            // >= 4.3 API 18
            WifiP2pGroup groupInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            printGroupInfo(groupInfo);
        }
    }

    private void wifiP2pThisDeviceChangedActionHandler(Intent intent) {
        // Log only
        String message;
        Date now = new Date();
        message = nContext.getString(R.string.wifi_p2p_setting_changes);
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_THIS_DEVICE_CHANGED_ACTION] " + message);
    }

    private void wifiP2pDiscoveryChangedActionHandler(Intent intent) {
        // Log only
        String message;
        Date now = new Date();
        int status = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 0);
        switch (status) {
            case WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED:
                message = nContext.getString(R.string.wifi_p2p_discovery_started);
                break;
            case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                message = nContext.getString(R.string.wifi_p2p_discovery_stopped);
                break;
            default:
                message = nContext.getString(R.string.this_is_a_bug);
                break;
        }
        Log.d(LOG_TAG, now.toString() + "[WIFI_P2P_DISCOVERY_CHANGED_ACTION] " + message);
        
        // Start Wifi P2P peer discovery if it's stopped
        if (intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
        	nManager.discoverPeers(nChannel, new WifiP2pManager.ActionListener() {
        		@Override
        		public void onSuccess() {
        			Log.d(LOG_TAG, new Date().toString() + " [RestartP2pPeerDiscovery] " + nContext.getString(R.string.wifi_p2p_ok));
        		}
        		@Override
        		public void onFailure(int reason) {
        			String message = getActionFailReason(reason);
        			Log.d(LOG_TAG, new Date().toString() + " [RestartP2pPeerDiscovery] " + message);
        		}
        	});
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String message;
        Date now = new Date();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            wifiP2pStateChangedActionHandler(intent);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            wifiP2pPeersChangedActionHandler(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            wifiP2pConnectionChangeActionHandler(intent);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            wifiP2pThisDeviceChangedActionHandler(intent);
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            wifiP2pDiscoveryChangedActionHandler(intent);
        } else {
            // Log only
            message = nContext.getString(R.string.this_is_a_bug);
            Log.d(this.getClass().getName(), now.toString() + "[Unknown action] " + message);
        }
    }

    private String getActionFailReason(int reason) {
        switch (reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return nContext.getString(R.string.wifi_p2p_unsupported);
            case WifiP2pManager.BUSY:
                return nContext.getString(R.string.wifi_p2p_busy);
            case WifiP2pManager.ERROR:
                return nContext.getString(R.string.wifi_p2p_error);
            case WifiP2pManager.NO_SERVICE_REQUESTS:
                return nContext.getString(R.string.wifi_p2p_no_service_request);
            default:
                return nContext.getString(R.string.this_is_a_bug);
        }
    }

}
