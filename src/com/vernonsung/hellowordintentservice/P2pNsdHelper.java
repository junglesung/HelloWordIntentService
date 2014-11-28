package com.vernonsung.hellowordintentservice;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

public class P2pNsdHelper {
	// Constant
	public static final String SERVICE_TYPE = "_holdme._tcp";
	private static final String LOG_TAG = "debug";
	
	// Variables
    private Context context;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDnsSdServiceInfo mServInfo;
    private WifiP2pManager.ActionListener addLocalServiceActionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(LOG_TAG, new Date().toString() + "[AddService] Wifi is OK");
        }
        @Override
        public void onFailure(int reason) {
            Log.d(LOG_TAG, new Date().toString() + "[AddService] " + getActionFailReason(reason));
        }
    };
    private WifiP2pManager.ActionListener delLocalServiceActionlistener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(LOG_TAG, new Date().toString() + "[DelService] Wifi is OK");
        }
        @Override
        public void onFailure(int reason) {
            Log.d(LOG_TAG, new Date().toString() + "[DelService] " + getActionFailReason(reason));
        }
    };
    private WifiP2pManager.ChannelListener mChannelListener = new WifiP2pManager.ChannelListener() {
        public void onChannelDisconnected() {
            Log.d(LOG_TAG, new Date().toString() + "onChannelDisconnected");
        }
    };

    public P2pNsdHelper(Context _context) {
        context = _context;
        mManager = (WifiP2pManager)_context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(_context, _context.getMainLooper(), mChannelListener);
    }

    public void createRoom(long _id) {
        HashMap<String, String> txtMap = new HashMap<String, String>();
        String name = String.valueOf(_id);
        txtMap.put("dummy", "dummy");
        txtMap.put("info", "info");
        mServInfo = WifiP2pDnsSdServiceInfo.newInstance(name, SERVICE_TYPE, txtMap);
        mManager.addLocalService(mChannel, mServInfo, addLocalServiceActionListener);
    }

//    public void resetRoom(String _name, String _ip, int _port) {
//        HashMap<String, String> txtMap = new HashMap<String, String>();
//        txtMap.put("name", _name);
//        txtMap.put("ip", _ip);
//        txtMap.put("port", String.valueOf(_port));
//        mServInfo = WifiP2pDnsSdServiceInfo.newInstance(_name, srvType, txtMap);
//        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                Log.d(LOG_TAG, new Date().toString() + "[ClearLocalServiceOnSuccess] Wifi is OK");
//                mManager.addLocalService(mChannel, mServInfo, addLocalServiceActionListener);
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Log.d(LOG_TAG, new Date().toString() + "[ClearLocalServiceOnFail] " + getActionFailReason(reason));
//            }
//        });
//    }

    public void deleteRoom() {
        if (mManager != null)
            mManager.removeLocalService(mChannel, mServInfo, delLocalServiceActionlistener);
    }

    @Override
    protected void finalize() throws Throwable {
        deleteRoom();
        super.finalize();
    }

    private String getActionFailReason(int reason) {
        switch (reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return context.getString(R.string.wifi_p2p_unsupported);
            case WifiP2pManager.BUSY:
                return context.getString(R.string.wifi_p2p_busy);
            case WifiP2pManager.ERROR:
                return context.getString(R.string.wifi_p2p_error);
            case WifiP2pManager.NO_SERVICE_REQUESTS:
                return context.getString(R.string.wifi_p2p_no_service_request);
            default:
                return context.getString(R.string.this_is_a_bug);
        }
    }

}
