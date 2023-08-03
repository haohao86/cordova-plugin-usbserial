package cordova.usbSerialAndroid;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import HelloWorld.Puppy;
import com.envotech.tablet.ExecuteResultKey;
import com.envotech.tablet.RFCommandApi;
import com.senter.support.openapi.StBarcodeScanner;
import com.senter.iot.support.openapi.StBarcodeScanOperator;
import android.content.Context;
import android.app.Activity;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android_serialport_api.SerialPortFinder;
import android_serialport_api.SerialPort;

import com.nlscan.ComAssistant.MyFunc;
import com.nlscan.ComAssistant.SerialHelper;
import com.nlscan.bean.AssistBean;
import com.nlscan.bean.ComBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileWriter;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import org.apache.cordova.CordovaActivity;

import utils.MyLog;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class echoes a string called from JavaScript.
 */
public class USBSerialAndroid extends CordovaPlugin {

	private RFCommandApi cmdApi;

	private Handler processHandler;

	private static int cmdTimeoutCounter;

	private SerialControl ComA;

	private DispQueueThread DispQueue;

	private AssistBean AssistData;

	private String code = "";

	private FileWriter outp = null;

	private int totalRepeatScan = 2;

	private Intent intent = new Intent();

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("helloWorld")) {

			Puppy helloworld = new Puppy();

			String message = args.getString(0) + " - 888";

			List<String> cmdList = new ArrayList<>();

			cmdList.add("0");

			cmdApi.getInstance().setContext(this.cordova.getActivity().getApplicationContext());

			// Select frequency first, 0 = 2410M
			cmdApi.getInstance().setCommandFrequency(0);

			cmdApi.getInstance().masterSendCommonCommand(args.getString(0), "000000", 530, cmdList);

			cmdTimeoutCounter = 0;

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					++cmdTimeoutCounter;

					int resultValue = cmdApi.getInstance().getCmdResult();

					if (resultValue != ExecuteResultKey.COMMAND_RESULT_NONE) {
						// command execute success
						if (resultValue == ExecuteResultKey.COMMAND_RESULT_SUCCESS) {
							callbackContext.success("Master Send Common Command Success");
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Communication with RF module failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMUNICATION_FAIL) {
							callbackContext.success("Communicate Fail");
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Command ERROR, Probably because the parameter is wrong
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMAND_FAIL) {
							callbackContext.success("Command error");
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Command execute failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_EXEC_FAIL) {
							callbackContext.success("Command Execute Fail");
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
					}
					// The user can customize the timeout
					else if (cmdTimeoutCounter >= 300) {
						callbackContext.success("Command Timeout");
						// stop current command
						cmdApi.getInstance().stopCurrentCommand();
						// stop timer
						processHandler.removeCallbacks(this);
					} else {
						processHandler.postDelayed(this, 100);
					}
				}
			};

			processHandler = new Handler();
			processHandler.postDelayed(runnable, 100);

			// this.coolMethod("hahaha " +
			// Integer.toString(cmdApi.getInstance().getCmdResult()), callbackContext);

			return true;
		} else if (action.equals("sendMasterCommand")) {

			intent.setAction("android.intent.action.ChangeHotonReceiver");
            this.cordova.getActivity().sendBroadcast(intent);
            intent.setAction("android.intent.action.lightonReceiver");
            this.cordova.getActivity().sendBroadcast(intent);

			cmdTimeoutCounter = 0;

			String masterSerialNumber = args.getString(0);
			String passowrd = args.getString(1);
			int command = Integer.parseInt(args.getString(2));
			List<String> cmdList = Arrays.asList(args.getString(3).split(","));

			cmdApi.getInstance().setContext(this.cordova.getActivity().getApplicationContext());
			cmdApi.getInstance().setCommandFrequency(0);
			cmdApi.getInstance().masterSendCommonCommand(masterSerialNumber, passowrd, command, cmdList);

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					++cmdTimeoutCounter;

					int resultValue = cmdApi.getInstance().getCmdResult();

					if (resultValue != ExecuteResultKey.COMMAND_RESULT_NONE) {
						// command execute success
						if (resultValue == ExecuteResultKey.COMMAND_RESULT_SUCCESS) {
							callbackContext.success(getResponse("Master Send Common Command Success"));
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Communication with RF module failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMUNICATION_FAIL) {
							callbackContext.error(getResponse("Communicate Fail"));
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Command ERROR, Probably because the parameter is wrong
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMAND_FAIL) {
							callbackContext.error(getResponse("Command error"));
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
						// Command execute failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_EXEC_FAIL) {
							callbackContext.error(getResponse("Command Execute Fail"));
							// stop current command
							cmdApi.getInstance().stopCurrentCommand();
							// stop timer
							processHandler.removeCallbacks(this);
						}
					}
					// The user can customize the timeout
					else if (cmdTimeoutCounter >= 300) {
						callbackContext.error(getResponse("Command Timeout"));
						// stop current command
						cmdApi.getInstance().stopCurrentCommand();
						// stop timer
						processHandler.removeCallbacks(this);
					} else {
						processHandler.postDelayed(this, 100);
					}
				}
			};

			processHandler = new Handler();
			processHandler.postDelayed(runnable, 100);

			// this.coolMethod(Integer.toString(cmdApi.getInstance().getCmdResult()),
			// callbackContext);

			return true;
		} else if (action.equals("laser")) {

			ComA = new SerialControl();
			DispQueue = new DispQueueThread();
			DispQueue.start();

			ComA.setPort("/dev/ttyMT2");
			ComA.setBaudRate("9600");
			OpenComPort(ComA);

			callbackContext.success("success");
			return true;
		} else if (action.equals("laserSendCommand")) {

			ComA.sendHex("1B31");
			code = "";
			int sent = 0;
			while (sent <= 30) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {

				}
				code = code.trim();
				if (!code.equals("") && !code.isEmpty() && code.length() > 0) {
					sent = 31;
					callbackContext.success(code.trim());
				} else if (sent == 15) {
					ComA.sendHex("1B31");
					sent = 0;
				}
				sent++;
			}
			return true;
		} else if (action.equals("laserClose")) {

			ComA.stopSend();
			ComA.close();
			callbackContext.success("success");
			return true;
		} else if (action.equals("laserScan")) {

			LaserScan(callbackContext);
			return true;
			
		} else if (action.equals("status")) {

			SerialPort serialPort = new SerialPort();
			if (serialPort.getSerialPortStatus("/dev/ttyMT2", 9600, 0) == true) {
				callbackContext.success("true");
			} else {
				callbackContext.success("false");
			}
			return true;
		} else if (action.equals("dongleClose")) {
			intent.setAction("android.intent.action.ChangeHotoffReceiver");
			this.cordova.getActivity().sendBroadcast(intent);
			intent.setAction("android.intent.action.lightoffReceiver");
			this.cordova.getActivity().sendBroadcast(intent);
			return true;
		} else if (action.equals("dongleOpen")) {
			intent.setAction("android.intent.action.ChangeHotonReceiver");
            this.cordova.getActivity().sendBroadcast(intent);
            intent.setAction("android.intent.action.lightonReceiver");
            this.cordova.getActivity().sendBroadcast(intent);
			return true;
		} else if (action.equals("laserScanV2")) {
			
			try {
				StBarcodeScanner scanner = StBarcodeScanner.getInstance();
				
				StBarcodeScanner.BarcodeInfo rslt = scanner.scanBarcodeInfo(this.cordova.getActivity().getApplication());
				
				int sent = 0;
				
				while (sent <= 30) {
					
					try {
						Thread.sleep(100);
					} catch (Exception e) {

					}
					
					if (rslt != null) {
						sent = 31;
						callbackContext.success(new String(rslt.getBarcodeValueAsBytes(), "utf-8"));
					} else if (sent == 15) {
						rslt = scanner.scanBarcodeInfo(this.cordova.getActivity().getApplication());
						sent = 0;
					}
					sent++;
				}
				
			} catch (Exception e) {
				
			}
			
			return true;
			
		} else if (action.equals("laserScanHoneyWell")) {
			
			StBarcodeScanOperator.getInstance(this.cordova.getActivity()).init(new StBarcodeScanOperator.InitListener() {
				@Override
				public void onInit(StBarcodeScanOperator.ErrorCode code) {
					//
					if (code == StBarcodeScanOperator.ErrorCode.SUCCESS) {
						scanHoneyWell(callbackContext);
					} else {
						callbackContext.success(new String("Init failed"));
					}
				}
			});
		
			return true;
		}
		return false;
	}

	private void scanHoneyWell(CallbackContext callbackContext) {
		
		try {
				
			StBarcodeScanOperator.BarcodeInfo barcodeInfo = StBarcodeScanOperator.getInstance(this.cordova.getActivity().getApplication()).scan();
			
			int sent = 0;
			
			while (sent <= 30) {
				
				try {
					Thread.sleep(100);
				} catch (Exception e) {

				}
				
				if (barcodeInfo != null && barcodeInfo.getBarcodeValueAsBytes() != null) {
					sent = 31;
					callbackContext.success(new String(barcodeInfo.getBarcodeValueAsBytes(), "utf-8"));
				} else if (sent == 15) {
					barcodeInfo = StBarcodeScanOperator.getInstance(this.cordova.getActivity().getApplication()).scan();
					sent = 0;
				}
				sent++;
			}

		} catch (Exception e) {
			
		}
	}
	
	private void coolMethod(String message, CallbackContext callbackContext) {
		if (message != null && message.length() > 0) {
			callbackContext.success(message);
		} else {
			callbackContext.error("Expected one non-empty string argument.");
		}
	}

	private String getResponse(String message) {
		return message;
	}

	private class SerialControl extends SerialHelper {

		public SerialControl() {
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData) {
			DispQueue.AddQueue(ComRecData);
		}
	}

	private void OpenComPort(SerialHelper ComPort) {
		try {
			ComPort.open();
			ShowMessage("Opening");
		} catch (SecurityException e) {
			ShowMessage("打开串口失败:没有串口读/写权限!");
		} catch (IOException e) {
			ShowMessage("打开串口失败:未知错误!");
		} catch (InvalidParameterException e) {
			ShowMessage("打开串口失败:参数错误!");
		}
	}

	protected void LaserScan(CallbackContext callbackContext) {
		intent.setAction("android.intent.action.ChangeHotonReceiver");
		this.cordova.getActivity().sendBroadcast(intent);
		intent.setAction("android.intent.action.lightonReceiver");
		this.cordova.getActivity().sendBroadcast(intent);

		ComA = new SerialControl();
		DispQueue = new DispQueueThread();
		DispQueue.start();

		ComA.setPort("/dev/ttyMT2");
		ComA.setBaudRate("9600");
		OpenComPort(ComA);

		ComA.sendHex("1B31");
		code = "";
		int sent = 0;
		int count = 0;

		while (sent <= 50) {

			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}

			code = code.trim();
			if (sent == 50 || (!code.equals("") && !code.isEmpty() && code.length() > 0)) {

				ComA.stopSend();
				ComA.close();
				intent.setAction("android.intent.action.ChangeHotoffReceiver");
				this.cordova.getActivity().sendBroadcast(intent);
				intent.setAction("android.intent.action.lightoffReceiver");
				this.cordova.getActivity().sendBroadcast(intent);

				sent = 51;
				callbackContext.success(code.trim());

			} else if (sent == 20) {
				ComA.sendHex("1B31");
				sent = 0;

				if (count == totalRepeatScan) {
					sent = 20;
				}
				count++;
			}
			sent++;
		}
	}

	private class DispQueueThread extends Thread {
		private Queue<ComBean> QueueList = new LinkedList<ComBean>();

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				final ComBean ComData;
				// code = "Step 1"+QueueList.size();
				while ((ComData = QueueList.poll()) != null) {
					new Thread(new Runnable() {
						public void run() {
							DispRecData(ComData);
						}
					});
					try {
						Thread.sleep(100);// 显示性能高的话，可以把此数值调小。
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		public synchronized void AddQueue(ComBean ComData) {
			code = new String(ComData.bRec);
			QueueList.add(ComData);
		}
	}

	private void DispRecData(ComBean ComRecData) {
		StringBuilder sMsg = new StringBuilder();
		sMsg.append(ComRecData.sRecTime);
		sMsg.append("[");
		sMsg.append(ComRecData.sComPort);
		sMsg.append("]");
		sMsg.append("[Txt] ");
		sMsg.append(new String(ComRecData.bRec));
		sMsg.append("[Hex] ");
		sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));

		sMsg.append("\r\n");
		code = sMsg.toString();
	}

	// ------------------------------------------显示消息
	private void ShowMessage(String sMsg) {
		MyLog.i("INFO", sMsg);
	}
}
