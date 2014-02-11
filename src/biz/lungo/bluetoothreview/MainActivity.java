package biz.lungo.bluetoothreview;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int REQUEST_CODE_DISCOVERABLE = 20;
	private static final int BLUETOOTH_ENABLE_REQEST_CODE = 74;
	Button myDev;
	Button sparDev;
	Button trueDev;
	TextView text, info;
	LinearLayout lin1, lin2;
	private BluetoothAdapter adapter;
	private String address, name;
	private int scanMode, state;
	private Set<BluetoothDevice> bondedDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myDev = (Button) findViewById(R.id.button1);
		sparDev = (Button) findViewById(R.id.button2);
		trueDev = (Button) findViewById(R.id.button3);
		text = (TextView) findViewById(R.id.textView2);
		info = (TextView) findViewById(R.id.textView1);
		lin1 = (LinearLayout) findViewById(R.id.LinearView1);
		lin2 = (LinearLayout) findViewById(R.id.LinearView2);
		text.setVisibility(View.GONE);
		adapter = BluetoothAdapter.getDefaultAdapter();
		bondedDevices = adapter.getBondedDevices();
		
		if (!adapter.isEnabled()) {
			Toast.makeText(this, "Yes", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BLUETOOTH_ENABLE_REQEST_CODE);

		} else {
			getBluetoothInfo();
			Toast.makeText(this, "No", Toast.LENGTH_LONG).show();
		}
		myDev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (text.getVisibility() == View.VISIBLE) {
					text.setVisibility(View.GONE);
				} else {
					text.setVisibility(View.VISIBLE);
				}
			}
		});
		sparDev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//lin1.addView(child);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BLUETOOTH_ENABLE_REQEST_CODE:
			if (requestCode == RESULT_OK) {
				getBluetoothInfo();
			} else {
				
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void getBluetoothInfo() {
		address = adapter.getAddress();
		name = adapter.getName();
		scanMode = adapter.getScanMode();
		state = adapter.getState();
		bondedDevices = adapter.getBondedDevices();
		String allInfo = address+" "+name+" "+scanMode+" "+state;
		text.setText(allInfo);
		for (BluetoothDevice device : bondedDevices){
			String deviceName = device.getName();
			TextView tv = new TextView(this);
			tv.setTag(device);
			tv.setText(deviceName);
			tv.setPadding(20, 0, 20, 20);
			tv.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					BluetoothDevice device = (BluetoothDevice) v.getTag();
					info.append(device.getName() + "\n");
					info.append(device.getUuids().toString() + "\n");
					info.append(device.getAddress() + "\n");
				}
			});
			lin2.addView(tv);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Discoverable");
		menu.add("Search devices");
		menu.add("Check");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if ("Discoverable".equals(item.getTitle())){
			Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20);
			startActivityForResult(i, REQUEST_CODE_DISCOVERABLE);
		}
		else if ("Search devices".equals(item.getTitle())){
			discoverRemoteDevices();
		}
		else if ("Check".equals(item.getTitle())){
			Toast.makeText(this, adapter.getScanMode() + "", Toast.LENGTH_LONG).show();
		}
		return true;
	}
	private void discoverRemoteDevices() {
		BroadcastReceiver makeDiscoverable = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
					Toast.makeText(getApplication(), "Scan started", Toast.LENGTH_LONG).show();
				}
				else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
					Toast.makeText(getApplication(), "Scan finished", Toast.LENGTH_LONG).show();			
				}
			}
		};
		registerReceiver(makeDiscoverable, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		registerReceiver(makeDiscoverable, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		
		BroadcastReceiver discoveryDevices = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String stringExtraName = remoteDevice.getName();
				Toast.makeText(getApplication(), "Device found: " + stringExtraName, Toast.LENGTH_LONG).show();
			}
		};
		registerReceiver(discoveryDevices, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		adapter.startDiscovery();
	}
}
