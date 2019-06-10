package ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nlscan.ComAssistant.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hidpos.UsbHidManager;
import utils.MyLog;


public class HidposFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.hid_open)
    Button hidOpen;
    @BindView(R.id.hid_close)
    Button hidClose;
    @BindView(R.id.hid_send_text)
    RadioButton hidSendText;
    @BindView(R.id.hid_send_hex)
    RadioButton hidSendHex;
    @BindView(R.id.hid_radio_btn)
    RadioGroup hidRadioBtn;
    @BindView(R.id.hid_et_send)
    EditText hidEtSend;
    @BindView(R.id.hid_send)
    Button hidSend;
    @BindView(R.id.separator)
    View separator;
    @BindView(R.id.hid_et_time_interval)
    EditText hidEtTimeInterval;
    @BindView(R.id.hid_btn_start_test)
    Button hidBtnStartTest;
    @BindView(R.id.hid_btn_stop_test)
    Button hidBtnStopTest;
    @BindView(R.id.hid_consoleText)
    TextView hidConsoleText;
    @BindView(R.id.hid_tv_time)
    TextView hidTvTime;
    Unbinder unbinder;

    public HidposFragment() {
        // Required empty public constructor
    }

    private int resCount = 1;

    String res = "";
    String rg_t = "text";
    boolean sendDataType = false;

    UsbHidManager usb_hid;

    Handler handler = new Handler();
    private int interval = 2000;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (hidConsoleText.getLineCount() > 500) {
                hidConsoleText.setText("");
            }
            handler.postDelayed(this, interval);
            hidSend.performClick();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("cpfalife","hidpos --- onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("cpfalife","hidpos --- onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hidpos, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("cpfalife","hidpos --- onActivityCreated");

        usb_hid = new UsbHidManager(getContext());

        hidOpen.setOnClickListener(this);
        hidClose.setOnClickListener(this);
        hidSend.setOnClickListener(this);
        hidBtnStartTest.setOnClickListener(this);
        hidBtnStopTest.setOnClickListener(this);
        hidBtnStopTest.setEnabled(false);

        //read data
        usb_hid.setiRlistener(new UsbHidManager.IReceiveDataListener() {

            @Override
            public void onReceiveData(String data) {
                String msg = getTime() + "resCount:" + resCount + " " + data + "\n";
                refreshView(msg);
//                mScrollView.smoothScrollTo(0, tv_res.getBottom());
                resCount++;
            }
        });

        hidRadioBtn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                switch (radioButtonId){
                    case R.id.hid_send_text:
                        sendDataType = false;
                        Log.i("cpfa","id:"+radioButtonId+"---text id："+R.id.hid_send_text);
                        break;
                    case R.id.hid_send_hex:
                        sendDataType = true;
                        Log.i("cpfa","id:"+radioButtonId+"---hex id："+R.id.hid_send_hex);
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hid_open:
                // usb hid device connect
                if(usb_hid.open()) {
                    hidConsoleText.setText("open success");
                    MyLog.i("hidpos","open success");}
                else {
                    hidConsoleText.setText("open fail");
                    MyLog.i("hidpos","open success");}
                break;
            case R.id.hid_close:
                //close
                if(usb_hid.close()) {
                    hidConsoleText.setText("close success");
                    MyLog.i("hidpos","close success");
                }
                else hidConsoleText.setText("close fail");
                break;
            case R.id.hid_send:
                //send command
                if (!TextUtils.isEmpty(hidEtSend.getText().toString())) {
                    usb_hid.sendData(hidEtSend.getText().toString(), sendDataType);
                }
                break;
            case R.id.hid_btn_start_test:
                //start test
                hidTvTime.setText(getTime());
                if (!TextUtils.isEmpty(hidEtTimeInterval.getText().toString())){
                    interval = Integer.valueOf(hidEtTimeInterval.getText().toString());
                }
                if (!TextUtils.isEmpty(hidEtSend.getText().toString())){
                    handler.postDelayed(runnable,interval);
                    hidBtnStopTest.setEnabled(true);
                    hidBtnStartTest.setEnabled(false);
                }else {
                    Toast.makeText(getContext(),"请输入指令", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.hid_btn_stop_test:
                //stop test
                handler.removeCallbacks(runnable);
                hidBtnStartTest.setEnabled(true);
                Toast.makeText(getContext(),"停止发送指令", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
}

    @Override
    public void onResume() {
        super.onResume();
        Log.i("cpfalife","hidpos --- onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("cpfalife","hidpos --- onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("cpfalife","hidpos --- onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Log.i("cpfalife","hidpos --- onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("cpfalife","hidpos --- onDestroy");
    }

    public String getTime(){
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
        Date date =  new Date(System.currentTimeMillis());
        String   time   =   formatter.format(date);
        return time;
    }

    void refreshView(String msg){
        hidConsoleText.append(msg);
        int offset=hidConsoleText.getLineCount()*hidConsoleText.getLineHeight();
        if(offset>hidConsoleText.getHeight()){
            hidConsoleText.scrollTo(0,offset-hidConsoleText.getHeight());
        }
    }
}
