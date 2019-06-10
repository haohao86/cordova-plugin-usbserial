package cdc;

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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import utils.MyLog;
import utils.clsPublic;

public class UsbCdcManager {

	static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	UsbManager usbManager;
	UsbDevice usbDevice = null;
	UsbInterface usbCdcInterface = null;
	UsbEndpoint usbCdcRead = null;
	UsbEndpoint usbCdcWrite = null;
	UsbDeviceConnection usbCdcConnection = null;

	Thread readThread = null;
	volatile boolean readThreadRunning = true;

    private static final int BUFSIZ = 4096;
	private final ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFSIZ);
	public static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
	byte[] myReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
    private static final boolean DEBUG = true;

    private static final int READ_WAIT_MILLIS = 200;
    
    CdcAcmSerialPort cdcSerial;

    private final ByteBuffer mWriteBuffer = ByteBuffer.allocate(BUFSIZ);

	PendingIntent permissionIntent;

	Context context;
	private USBThreadReadDataReceiver usbThreadReadDataReceiver = null;
	private IReceiveDataListener iRlistener;

	private final Handler uiHandler = new Handler();

	public UsbCdcManager(Context context) {
		this.context = context;
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		cdcSerial = new CdcAcmSerialPort(usbDevice);
	}

	// 2.open usb device
	public boolean open() {

		// check if there's a connected usb device
		if (usbManager.getDeviceList().isEmpty()) {
			MyLog.i("cdc","No connected devices");
			return false; 
		}

		if (usbDevice == null) {
			permissionIntent = PendingIntent.getBroadcast(context, 0,
					new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			context.registerReceiver(usbReceiver, filter);
			MyLog.i("cdc","register success");
			// get the first (only) connected device
		}

		// get the first (only) connected device
		usbDevice = usbManager.getDeviceList().values().iterator().next();

		// user must approve of connection if not in the
		// /res/usb_device_filter.xml file
		usbManager.requestPermission(usbDevice, permissionIntent);
		MyLog.i("cdc","1  requestPermission");
		return true;
	}

	// 3. cdc endpoint
	public void setCdcConnection() {
		// check if there's a connected usb device
		if (usbManager.getDeviceList().isEmpty()) {
			MyLog.i("cdc","setCdcConnection: No connected devices");
			return;
		}

		usbCdcConnection = usbManager.openDevice(usbDevice);
		Log.d("cpfa", "usbCdcConnection ? :" + usbCdcConnection);
		// find the right interface
		for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
			// communications device class (HID) type device
			if (usbDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA || usbDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC) {
				usbCdcInterface = usbDevice.getInterface(i);
				MyLog.i("cdc","find interface-----" + i);
			}
		}
		if (null == usbCdcConnection) {
			MyLog.i("cdc","usbCdcConnection is null");
			throw new IllegalArgumentException("not connect find");
		} else {
			usbCdcConnection.claimInterface(usbCdcInterface, true);
			MyLog.i("cdc","claimInterface success");
		}
		// find the endpoints
		
		UsbEndpoint epOut = null;
		UsbEndpoint epIn = null;
		// look for our bulk endpoints
		for (int i = 0; i < usbCdcInterface.getEndpointCount(); i++) {
			UsbEndpoint ep = usbCdcInterface.getEndpoint(i);
			if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
				if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
					epOut = ep;  //write is OUT (host to device)
					Log.i("cpfa", "epout :" + i);
				} else {
					epIn = ep;   //read is IN (device to host)
				}
			}
		}

		if (epOut == null || epIn == null) {
			throw new IllegalArgumentException("not all endpoints found");
		}
		
		usbCdcWrite = epOut;
		usbCdcRead = epIn;

		usbThreadReadDataReceiver = new USBThreadReadDataReceiver();
		usbThreadReadDataReceiver.start();
		if(usbThreadReadDataReceiver.isStopped()){
			Log.i("cpfa","isstop: ture");
		}else{
			Log.i("cpfa","isstop: false");
		}

		MyLog.i("cdc","2  set endpoint success");
	}

	public boolean close() {
		// check if there's a connected usb device
		if (usbManager.getDeviceList().isEmpty()) {
			MyLog.i("cdc","No connected devices");
			return false;
		}

		if (usbDevice != null) {

			if (usbThreadReadDataReceiver != null) {
				usbThreadReadDataReceiver.stopThis();
			}

			usbCdcConnection.releaseInterface(usbCdcInterface);
			usbCdcConnection.close();
			usbCdcConnection = null;
//			usbCdcInterface = null;
//			usbCdcRead = null;
//			usbCdcWrite = null;
			usbDevice = null;

			context.unregisterReceiver(usbReceiver);

			MyLog.i("cdc","USB connection closed");
			return true;
		}
		return true;
	}

	public int sendData(String data, boolean sendAsString) {
		if (usbDevice != null && usbCdcWrite != null
				&& usbManager.hasPermission(usbDevice) && !data.isEmpty()) {
			// mLog(connection +"\n"+ device +"\n"+ request +"\n"+
			// packetSize);
			byte[] out = data.getBytes();// UTF-16LE
											// Charset.forName("UTF-16")

			Log.i("cpfa", "senddata text:" + clsPublic.Bytes2HexString(out));

			if (sendAsString) {
				try {
					String str[] = data.split("[\\s]");
					
					out = new byte[str.length];
					for (int i = 0; i < str.length; i++) {
						out[i] = clsPublic.toByte(Integer.decode(str[i]));
					}
					Log.i("cpfa",
							"senddata hex:" + clsPublic.Bytes2HexString(out));
				} catch (Exception e) {
					// onSendingError(e);
				}
			}

			Log.i("cpfa", "senddata final:" + clsPublic.Bytes2HexString(out));
			int status = usbCdcConnection.bulkTransfer(usbCdcWrite, out,
					out.length, 150);
			return status;
		} else {
			return -1;
		}
	}
	
	/*
	 * start copy cdc manager 
	 */
	 public void writeAsync(byte[] data) {
	        synchronized (mWriteBuffer) {
	            mWriteBuffer.put(data);
	        }
	    };
	    
		private class USBThreadReadDataReceiver extends Thread {

			private volatile boolean isStopped;

			public USBThreadReadDataReceiver() {
			}
			@Override
			public void run() {
				try {
					if (usbCdcConnection != null && usbCdcRead != null) {
						String dataS = "";
						// ����inpoint
						while (!isStopped) {
							step();
						}
					}
				} catch (Exception e) {
					MyLog.e("cdc", "Error in receive thread:" + e);
				}
			}

			public boolean isStopped() {
				return isStopped;
			}

			public void stopThis() {
				isStopped = true;
			}

			public void startThis() {
				isStopped = false;
			}
		}


	    private void step() throws IOException {
	        // Handle incoming data.
	        int len = cdcSerial.read(mReadBuffer.array(), READ_WAIT_MILLIS,usbCdcConnection,usbCdcRead);
	        if (len > 0) {
	            if (DEBUG) Log.d("cpfa", "Read data len=" + len);
	                Log.d("cpfa", "mReadBuffer len=" + mReadBuffer.capacity());
	                final byte[] data = new byte[len];
	                mReadBuffer.get(data, 0, len);
	                Log.i("cpfa", "read data = " + clsPublic.Bytes2HexString(data));
	                final StringBuilder stringBuilder = new StringBuilder();
					for (int i = 0; i < data.length && data[i] != 0; i++) {
						stringBuilder.append(String
								.valueOf((char) data[i]));
					}
					
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
	                iRlistener.onReceiveData(stringBuilder.toString());
						}
					});
	            mReadBuffer.clear();
	        }

	        // Handle outgoing data.
	        byte[] outBuff = null;
	        synchronized (mWriteBuffer) {
	            len = mWriteBuffer.position();
	            if (len > 0) {
	                outBuff = new byte[len];
	                mWriteBuffer.rewind();
	                mWriteBuffer.get(outBuff, 0, len);
	                mWriteBuffer.clear();
	            }
	        }
	        if (outBuff != null) {
	            if (DEBUG) {
	                Log.d("cpfa", "Writing data len=" + len);
	            }
	            cdcSerial.write(outBuff, READ_WAIT_MILLIS);
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


	/*
	usb信息
	 */
	private String listUsbDevices() {
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

		if (deviceList.size() == 0) {
			return "no usb devices found";
		}

		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		String returnValue = "";
		UsbInterface usbInterface;

		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			returnValue += "Name: " + device.getDeviceName();
			returnValue += "\nID: " + device.getDeviceId();
			returnValue += "\nProtocol: " + device.getDeviceProtocol();
			returnValue += "\nClass: " + device.getDeviceClass();
			returnValue += "\nSubclass: " + device.getDeviceSubclass();
			returnValue += "\nProduct ID: " + device.getProductId();
			returnValue += "\nVendor ID: " + device.getVendorId();
			returnValue += "\nInterface count: " + device.getInterfaceCount();

			for (int i = 0; i < device.getInterfaceCount(); i++) {
				usbInterface = device.getInterface(i);
				returnValue += "\n  Interface " + i;
				returnValue += "\n\tInterface ID: " + usbInterface.getId();
				returnValue += "\n\tClass: " + usbInterface.getInterfaceClass();
				returnValue += "\n\tProtocol: "
						+ usbInterface.getInterfaceProtocol();
				returnValue += "\n\tSubclass: "
						+ usbInterface.getInterfaceSubclass();
				returnValue += "\n\tEndpoint count: "
						+ usbInterface.getEndpointCount();

				for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
					returnValue += "\n\t  Endpoint " + j;
					returnValue += "\n\t\tAddress: "
							+ usbInterface.getEndpoint(j).getAddress();
					returnValue += "\n\t\tAttributes: "
							+ usbInterface.getEndpoint(j).getAttributes();
					returnValue += "\n\t\tDirection: "
							+ usbInterface.getEndpoint(j).getDirection();
					returnValue += "\n\t\tNumber: "
							+ usbInterface.getEndpoint(j).getEndpointNumber();
					returnValue += "\n\t\tInterval: "
							+ usbInterface.getEndpoint(j).getInterval();
					returnValue += "\n\t\tType: "
							+ usbInterface.getEndpoint(j).getType();
					returnValue += "\n\t\tMax packet size: "
							+ usbInterface.getEndpoint(j).getMaxPacketSize();
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
						setCdcConnection();
						MyLog.d("cdc", "Permission passed for USB device");
					} else {
						MyLog.d("cdc", "Permission denied for USB device");
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if (usbDevice != null) {
					usbCdcConnection.releaseInterface(usbCdcInterface);
					usbCdcConnection.close();
					usbCdcConnection = null;
					usbDevice = null;
					
					if (usbThreadReadDataReceiver != null) {
						usbThreadReadDataReceiver.stopThis();
					}
					MyLog.d("cdc", "USB device detached");
				}
			}
		}
	};
}
