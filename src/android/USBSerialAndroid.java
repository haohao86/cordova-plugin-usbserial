package cordova.usbSerialAndroid;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import HelloWorld.Puppy;
import com.envotech.tablet.ExecuteResultKey;
import com.envotech.tablet.RFCommandApi;

import android.content.Context;
import android.app.Activity;

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
			
            this.coolMethod("hahaha " + Integer.toString(cmdApi.getInstance().getCmdResult()), callbackContext);
			
            return true;
        } else if (action.equals("sendMasterCommand")) {
			
			String masterSerialNumber = args.getString(0);
			String passowrd = args.getString(1);
			int command = Integer.parseInt(args.getString(2));
			List<String> cmdList = Arrays.asList(args.getString(3).split(","));
			
			cmdApi.getInstance().setContext(this.cordova.getActivity().getApplicationContext());
			cmdApi.getInstance().setCommandFrequency(0);
			cmdApi.getInstance().masterSendCommonCommand(masterSerialNumber, passowrd, command, cmdList);
			
			this.coolMethod(Integer.toString(cmdApi.getInstance().getCmdResult()), callbackContext);
			
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
}
