package cordova.usbSerialAndroid;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import HelloWorld.Puppy;
import com.envotech.tablet.ExecuteResultKey;
import com.envotech.tablet.RFCommandApi;

import android.content.Context;
import android.app.Activity;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class USBSerialAndroid extends CordovaPlugin {

	private RFCommandApi cmdApi;
	
	private Handler processHandler;
	
	private static int cmdTimeoutCounter;
	
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("helloWorld")) {
			
			Puppy helloworld = new Puppy();
			
            String message = args.getString(0) + " - 888";
			
			List<String> cmdList = new ArrayList<>();
 
			cmdList.add("0");
			
			cmdApi.getInstance().setContext(this.cordova.getActivity().getApplicationContext());
 
			//Select frequency first, 0 = 2410M
			cmdApi.getInstance().setCommandFrequency(0);
			
			cmdApi.getInstance().masterSendCommonCommand("AC011244", "000000", 530, cmdList);
			
			cmdTimeoutCounter = 0;
			
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					++cmdTimeoutCounter;

					int resultValue = cmdApi.getInstance().getCmdResult();

					if (resultValue != ExecuteResultKey.COMMAND_RESULT_NONE) {
						//command execute success
						if (resultValue == ExecuteResultKey.COMMAND_RESULT_SUCCESS) {
							callbackContext.success("Master Send Common Command Success");
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Communication with RF module failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMUNICATION_FAIL) {
							callbackContext.success("Communicate Fail");
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Command ERROR, Probably because the parameter is wrong
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMAND_FAIL) {
							callbackContext.success("Command error");
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Command execute failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_EXEC_FAIL) {
							callbackContext.success("Command Execute Fail");
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
					}
					// The user can customize the timeout
					else if (cmdTimeoutCounter >= 300) {
						callbackContext.success("Command Timeout");
						//stop current command
						cmdApi.getInstance().stopCurrentCommand();
						//stop timer
						processHandler.removeCallbacks(this);
					} else {
						processHandler.postDelayed(this, 100);
					}
				}
			};
			
			processHandler = new Handler();
			processHandler.postDelayed(runnable, 100);
			
            //this.coolMethod("hahaha " + Integer.toString(cmdApi.getInstance().getCmdResult()), callbackContext);
			
            return true;
        } else if (action.equals("sendMasterCommand")) {
			
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
						//command execute success
						if (resultValue == ExecuteResultKey.COMMAND_RESULT_SUCCESS) {
							callbackContext.success(getResponse("Master Send Common Command Success"));
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Communication with RF module failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMUNICATION_FAIL) {
							callbackContext.error(getResponse("Communicate Fail"));
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Command ERROR, Probably because the parameter is wrong
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_COMMAND_FAIL) {
							callbackContext.error(getResponse("Command error"));
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
						//Command execute failed
						else if (resultValue == ExecuteResultKey.COMMAND_RESULT_EXEC_FAIL) {
							callbackContext.error(getResponse("Command Execute Fail"));
							//stop current command
							cmdApi.getInstance().stopCurrentCommand();
							//stop timer
							processHandler.removeCallbacks(this);
						}
					}
					// The user can customize the timeout
					else if (cmdTimeoutCounter >= 300) {
						callbackContext.error(getResponse("Command Timeout"));
						//stop current command
						cmdApi.getInstance().stopCurrentCommand();
						//stop timer
						processHandler.removeCallbacks(this);
					} else {
						processHandler.postDelayed(this, 100);
					}
				}
			};
			
			processHandler = new Handler();
			processHandler.postDelayed(runnable, 100);
			
			//this.coolMethod(Integer.toString(cmdApi.getInstance().getCmdResult()), callbackContext);
			
            return true;
		}
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
	
	private String getResponse(String message) {
		return '{"message": "' + message + '"}';
	}
}
