package thu.kejiafan.mobinet;

import thu.kejiafan.mobinet.R.id;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class NetworkFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.network, container, false);
		initWidget(view);
		handler4Wifi.post(runnable4Wifi);
		
		return view;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub		
		super.onPause();
		
		Config.statistics.pageviewEnd(this, "NetworkFragment");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		
		Config.statistics.pageviewStart(this, "NetworkFragment");
	}
	
	private void initWidget(View view) {
    	Config.btnRun = (Button) view.findViewById(id.btnRun);
    	Config.tvDataConnectionState = (TextView) view.findViewById(id.dataConnection);
    	Config.tvWiFiConnection = (TextView) view.findViewById(id.wifiConnection);
		Config.tvMacAddress = (TextView) view.findViewById(id.macAddress);
		Config.tvIPAddress = (TextView) view.findViewById(id.ipAddress); 
		Config.tvWiFiInfo = (TextView) view.findViewById(id.wifiInfo);
		Config.tvPingLatency = (TextView) view.findViewById(id.pingLatency);
		Config.tvDNSLatency = (TextView) view.findViewById(id.dnsLatency);
		Config.tvTestReport = (TextView) view.findViewById(id.testState);
		Config.tvThroughput = (TextView) view.findViewById(id.throughput);
	    
	    Config.btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub

				if (Config.wifiState.equals("Disconnected")
						&& Config.dataConnectionState.equals("Disconnected")) {
					Config.tvTestReport.setText("�����ѶϿ���������������");
					return;
				}

				Config.testFlag = 0;

				Config.mDialog = new ProgressDialog(getActivity());
				Config.mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				Config.mDialog.setTitle("MobiNet");
				Config.mDialog.setIcon(R.drawable.ic_launcher);
				Config.mDialog.setMessage("testing...");
				Config.mDialog.setIndeterminate(false);
				Config.mDialog.setCancelable(false);
				Config.mDialog.show();
				
				Config.tvTestReport.setText("DNS lookup testing...");
				Config.mDialog.setMessage("DNS lookup testing...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler4Test.removeCallbacks(runnable4Test);
				handler4Test.post(runnable4Test);
				Measurement.dnsLookupTest(Config.testServerip, 10);
				
				Config.tvTestReport.setText("Ping testing...");
				Config.mDialog.setMessage("Ping testing...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler4Test.removeCallbacks(runnable4Test);
				handler4Test.post(runnable4Test);
				Measurement.pingCmdTest(Config.testServerip, 10);							
				
//				handler4Show.post(runnable4Show);

//				Config.tvTestReport.setText("TCP uplink testing...");
//				pDialog.setMessage("TCP uplink testing...");
//				Config.myTcpTest = new TCPTest(mHandler, serverIPString, "2",
//						"5", Config.fosUplink, 2);
//				handler4Show.post(runnable4Show);
			}

		});
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				Config.btnRun.setEnabled(false);
				Config.tvTestReport.setText("Connecting...");
			} else if (msg.what == 1) {
				Config.tvTestReport.setText("Client has connected to server");
			} else if (msg.what == 2) {
				Config.tvUpload.setText("Reconnecting...");
			} else if (msg.what == 3) {
				Config.tvTestReport.setText("TCP downlink test finished");
				Config.mDialog.dismiss();
				Config.mDialog.cancel();
				handler4Show.removeCallbacks(runnable4Show);
				Config.tvThroughput.setText("ƽ������:"
						+ Config.myTcpTest.mAvgDownlinkThroughput + " kbps");
//				Config.tvThroughput.setText("ƽ������:"
//						+ Config.myTcpTest.mAvgUplinkThroughput + " ƽ������:"
//						+ Config.myTcpTest.mAvgDownlinkThroughput + " kbps");
				Config.btnRun.setEnabled(true);
			} else if (msg.what == 4) {
				Config.tvUpload.setText("Server maybe have some error");
				Config.btnRun.setEnabled(true);
			}
		};
	};
	
	private Handler handler4Show = new Handler();

	private Runnable runnable4Show = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Show.postDelayed(runnable4Show, 1000);
			Config.mDialog.setMessage("Downlink throughput: " + Config.myTcpTest.mDownlinkThroughput + " kbps");
			Config.tvThroughput.setText("����:"
					+ Config.myTcpTest.mDownlinkThroughput + " kbps");
//			Config.tvThroughput.setText("����:" + Config.myTcpTest.mUplinkThroughput + " ����:"
//					+ Config.myTcpTest.mDownlinkThroughput + " kbps");
		}
	};
	
	private Handler handler4Wifi = new Handler();

	private Runnable runnable4Wifi = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Config.tvDataConnectionState.setText(Config.dataConnectionState);
			/**
			 * �Ƿ�����Wifi
			 */
			try {
				ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

				if (activeNetInfo != null
						&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					
					WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					Config.wifiState = "Connected:" + wifiInfo.getSSID();
					Config.wifiInfo = "RSSI:" + wifiInfo.getRssi() + " LinkSpeed:" + wifiInfo.getLinkSpeed();
					Config.macAddress = wifiInfo.getMacAddress();
					Config.ipAddress = SignalUtil.int2IP(wifiInfo.getIpAddress());
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.tvMacAddress.setText(Config.macAddress);
					Config.tvIPAddress.setText(Config.ipAddress);
					
				} else {
					Config.wifiState = "Disconnected";
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.wifiInfo = "WiFi���Ӻ���Ч";
				}	
				Config.tvWiFiInfo.setText(Config.wifiInfo);
			} catch (Exception e) {
				// TODO: handle exception
			}

			handler4Wifi.postDelayed(runnable4Wifi, 5000);
		}
	};
	
	private Handler handler4Test = new Handler();

	private Runnable runnable4Test = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Test.postDelayed(runnable4Test, 1000);
			if (Config.testFlag == 11) {
				Config.tvPingLatency.setText(Config.pingInfo + " ms");
				Config.mDialog.setMessage("Ping latency: " + Config.pingInfo + " ms");
				Config.tvTestReport.setText("Ping test finished");
				Config.testFlag = 10;
				
				Config.tvTestReport.setText("TCP downlink testing...");
				Config.mDialog.setMessage("TCP downlink testing...");
				Config.myTcpTest = new TCPTest(mHandler, Config.testServerip, "1",
						"5", Config.fosDownlink, 1);
				handler4Show.post(runnable4Show);
			} else if (Config.testFlag == 12) {
				Config.mDialog.setMessage("Ping test failed");
				Config.tvTestReport.setText("Ping test failed");
				Config.testFlag = 10;
				
				Config.tvTestReport.setText("TCP downlink testing...");
				Config.mDialog.setMessage("TCP downlink testing...");
				Config.myTcpTest = new TCPTest(mHandler, Config.testServerip, "2",
						"5", Config.fosDownlink, 1);
				handler4Show.post(runnable4Show);
			} else if (Config.testFlag == 13) {
				Config.tvTestReport.setText("���Ļ����ݲ�֧�� ���ע�����汾");
				Config.testFlag = 10;
			} else if (Config.testFlag == 21) {
				Config.tvDNSLatency.setText(Config.dnsLookupInfo + " ms");
				Config.mDialog.setMessage("DNS lookup latency: " + Config.dnsLookupInfo + " ms");
				Config.tvTestReport.setText("DNS lookup test finished");
				Config.testFlag = 20;
			} else if (Config.testFlag == 22) {
				Config.mDialog.setMessage("DNS lookup test failed");
				Config.tvTestReport.setText("DNS lookup test failed");
				Config.testFlag = 20;
			}
		}
	};
}