package hidpos;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;

import utils.MyLog;
import utils.clsPublic;

public class UsbHidManager {
	
	static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	UsbManager usbManager;
	UsbDevice usbDevice = null;
	UsbInterface usbHidInterface = null;
	UsbEndpoint usbHidRead = null;
	UsbEndpoint usbHidWrite = null;
	UsbDeviceConnection usbHidConnection;

	Thread readThread = null;
	volatile boolean readThreadRunning = true;

	PendingIntent permissionIntent;

	Context context;
	private int packetSize;
	private USBThreadDataReceiver usbThreadDataReceiver;
	private IReceiveDataListener iRlistener;
	
	private final Handler uiHandler = new Handler();

	public UsbHidManager(Context context)
	{
		this.context = context;
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	}
	
	
	//2.open usb device
	public boolean open()
	{
		// check if there's a connected usb device
				if (usbManager.getDeviceList().isEmpty()) {
					MyLog.i("hidpos","No connected devices");
					return false;
				}
				
		if(usbDevice == null){
			permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			context.registerReceiver(usbReceiver, filter);
			MyLog.i("hidpos","register success");
			// get the first (only) connected device
		}
		  usbDevice = usbManager.getDeviceList().values().iterator().next();
		  // user must approve of connection if not in the /res/usb_device_filter.xml file 
		  usbManager.requestPermission(usbDevice, permissionIntent);
		  MyLog.i("hidpos","1  connect success");
		  return true;
	}
	
	//3. hid endpoint
		private void setHidConnection() {
			// check if there's a connected usb device
			if (usbManager.getDeviceList().isEmpty()) {
				MyLog.i("hidpos","No connected devices");
				return;
			}
			usbHidConnection = usbManager.openDevice(usbDevice);
			
			// find the right interface
			for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
				// communications device class (HID) type device
				if (usbDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
					usbHidInterface = usbDevice.getInterface(i);
					MyLog.i("hidpos", "find interface-----" + i);
				}
				if(null == usbHidConnection){
					MyLog.i("hidpos", "usbHidConnection is null");
				}else{
				usbHidConnection.claimInterface(usbHidInterface, true);
				}
					// find the endpoints
					
					if (usbHidInterface.getEndpoint(1) != null) {
						usbHidWrite = usbHidInterface.getEndpoint(1);
					}
					if (usbHidInterface.getEndpoint(0) != null) {
						usbHidRead = usbHidInterface.getEndpoint(0);
						packetSize = usbHidRead.getMaxPacketSize();
					}
				}

			MyLog.i("hidpos", "2  set endpoint success");
			usbThreadDataReceiver = new USBThreadDataReceiver();
			usbThreadDataReceiver.start();
		}

		
	public boolean close()
	{
		
		// check if there's a connected usb device
		if (usbManager.getDeviceList().isEmpty()) {
			MyLog.i("hidpos","No connected devices");
			return false;
		}
		
		if (usbDevice != null) {
			
			if (usbThreadDataReceiver != null) {
				usbThreadDataReceiver.stopThis();
				}
			
			usbHidConnection.releaseInterface(usbHidInterface);
			usbHidConnection.close();
			usbHidConnection = null;
			usbHidInterface = null;
			usbHidRead = null;
			usbHidWrite = null;
			usbDevice = null;
			
			context.unregisterReceiver(usbReceiver);
			MyLog.i("hidpos", "USB connection closed");
			return true;
		}
		return true;
	}
	
	public int sendData(String data, boolean sendAsString) {
		if (usbDevice != null && usbHidWrite != null && usbManager.hasPermission(usbDevice) && !data.isEmpty()) {
			// mLog(connection +"\n"+ device +"\n"+ request +"\n"+
			// packetSize);
			byte[] out = data.getBytes();// UTF-16LE
											// Charset.forName("UTF-16")

			byte[] out1 = new byte[out.length+2];
			out1[0] = 0x04;
			out1[1] = clsPublic.toByte(out.length);
			Log.i("cpfa","out.length:"+out.length);

			for(int i = 0;i<out.length;i++){
				out1[i+2] = out[i];
			}
			Log.i("cpfa","out1 11111:"+clsPublic.Bytes2HexString(out1));
			out = new byte[out1.length];
			for(int j = 0;j<out1.length;j++){
				out[j] = out1[j];
			}

				Log.i("cpfa","senddata text:"+clsPublic.Bytes2HexString(out));


			if (sendAsString) {
				try {
					String str[] = data.split("[\\s]");
					out = new byte[str.length+2];
					out[0] = 0x04;
					out[1] = clsPublic.toByte(Integer.decode(Integer.toHexString(str.length)));
					for (int i = 0; i < str.length; i++) {
						out[i+2] = clsPublic.toByte(Integer.decode(str[i]));
					}
					Log.i("cpfa","senddata hex:"+clsPublic.Bytes2HexString(out));
				} catch (Exception e) {
//					onSendingError(e);
				}
			}

			Log.i("cpfa","senddata final:"+clsPublic.Bytes2HexString(out));
			int status = usbHidConnection.bulkTransfer(usbHidWrite, out, out.length, 250);
			return status;
		}else{
		return -1;
		}
	}
	
	private class USBThreadDataReceiver extends Thread {

		private volatile boolean isStopped;

		public USBThreadDataReceiver() {
		}

		@Override
		public void run() {
			try {
				if (usbHidConnection != null && usbHidRead != null) {
					String dataS = "";
					//inpoint
					while (!isStopped) {
						final byte[] buffer = new byte[packetSize];
						final byte last = 0x01;

						final int status = usbHidConnection.bulkTransfer(usbHidRead, buffer, packetSize, 100);
						if (status > 0) {
							//hid-pos
							int datalen = clsPublic.toInt(buffer[1]);
							Log.i("cpfa","datalen:-----"+datalen);
							String s = new String(buffer,2,datalen,"ASCII");
							dataS +=s;
							Log.i("cpfa","string dataS = "+dataS);
							final byte[] buff = dataS.getBytes();
							//���buffer���һλ��00����ִ��post
							if(last != buffer[63]) {
								dataS = "";
								final StringBuilder stringBuilder = new StringBuilder();
								for (int i = 0; i < buff.length && buff[i] != 0; i++) {
									stringBuilder.append(String.valueOf((char) buff[i]));
								}
								uiHandler.post(new Runnable() {
									@Override
									public void run() {
//										onUSBDataReceive(buff);
										iRlistener.onReceiveData(stringBuilder.toString());
										Log.i("cpfa", "buff :-----" + clsPublic.Bytes2HexString(buff));
										Log.i("cpfa", "buff length :-----" + buff.length);
									}
								});
							}
						}
					}
				}
			} catch (Exception e) {
				MyLog.e("ERROR","Error in receive thread :" + e);
			}
		}

		public void stopThis() {
			isStopped = true;
		}
	}
	
	public interface IReceiveDataListener {
		void onReceiveData(String data);
	}
	
	
	public IReceiveDataListener getiRlistener() {
		return iRlistener;
	}


	public void setiRlistener(IReceiveDataListener iRlistener) {
		this.iRlistener = iRlistener;
	}


	private String listUsbDevices()
	{
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

		if(deviceList.size() == 0)
		{
			return "no usb devices found";
		}

		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		String returnValue = "";
		UsbInterface usbInterface;

		while(deviceIterator.hasNext())
		{
			UsbDevice device = deviceIterator.next();
			returnValue += "Name: " + device.getDeviceName();
			returnValue += "\nID: " + device.getDeviceId();
			returnValue += "\nProtocol: " + device.getDeviceProtocol();
			returnValue += "\nClass: " + device.getDeviceClass();
			returnValue += "\nSubclass: " + device.getDeviceSubclass();
			returnValue += "\nProduct ID: " + device.getProductId();
			returnValue += "\nVendor ID: " + device.getVendorId();
			returnValue += "\nInterface count: " + device.getInterfaceCount();

			for(int i = 0; i < device.getInterfaceCount(); i++)
			{
				usbInterface = device.getInterface(i);
				returnValue += "\n  Interface " + i;
				returnValue += "\n\tInterface ID: " + usbInterface.getId();
				returnValue += "\n\tClass: " + usbInterface.getInterfaceClass();
				returnValue += "\n\tProtocol: " + usbInterface.getInterfaceProtocol();
				returnValue += "\n\tSubclass: " + usbInterface.getInterfaceSubclass();
				returnValue += "\n\tEndpoint count: " + usbInterface.getEndpointCount();

				for(int j = 0; j < usbInterface.getEndpointCount(); j++)
				{
					returnValue += "\n\t  Endpoint " + j;
					returnValue += "\n\t\tAddress: " + usbInterface.getEndpoint(j).getAddress();
					returnValue += "\n\t\tAttributes: " + usbInterface.getEndpoint(j).getAttributes();
					returnValue += "\n\t\tDirection: " + usbInterface.getEndpoint(j).getDirection();
					returnValue += "\n\t\tNumber: " + usbInterface.getEndpoint(j).getEndpointNumber();
					returnValue += "\n\t\tInterval: " + usbInterface.getEndpoint(j).getInterval();
					returnValue += "\n\t\tType: " + usbInterface.getEndpoint(j).getType();
					returnValue += "\n\t\tMax packet size: " + usbInterface.getEndpoint(j).getMaxPacketSize();
				}
			}
		}

		return returnValue;
	}


	private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				// broadcast is like an interrupt and works asynchronously with
				// the class, it must be synced just in case
				synchronized (this) {
					usbDevice = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						// fetch all the endpoints
						// setupConnection();
						setHidConnection();
						
					} else {
						MyLog.i("hidpos", "Permission denied for USB device");
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if (usbDevice != null) {
					usbHidConnection.releaseInterface(usbHidInterface);
					usbHidConnection.close();
					usbHidConnection = null;
					usbDevice = null;
					
				if (usbThreadDataReceiver != null) {
					usbThreadDataReceiver.stopThis();
					}

					MyLog.i("hidpos", "USB connection closed");
				}
			}
		}
	};
	
}
