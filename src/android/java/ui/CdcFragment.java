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
import cdc.UsbCdcManager;
import utils.MyLog;


public class CdcFragment extends Fragment implements View.OnClickListener{

    @BindView(R.id.cdc_open)
    Button cdcOpen;
    @BindView(R.id.cdc_close)
    Button cdcClose;
    @BindView(R.id.cdc_send_text)
    RadioButton cdcSendText;
    @BindView(R.id.cdc_send_hex)
    RadioButton cdcSendHex;
    @BindView(R.id.cdc_radio_btn)
    RadioGroup cdcRadioBtn;
    @BindView(R.id.cdc_et_send)
    EditText cdcEtSend;
    @BindView(R.id.cdc_send)
    Button cdcSend;
    @BindView(R.id.cdc_et_time_interval)
    EditText cdcEtTimeInterval;
    @BindView(R.id.cdc_btn_start_test)
    Button cdcBtnStartTest;
    @BindView(R.id.cdc_btn_stop_test)
    Button cdcBtnStopTest;
    @BindView(R.id.cdc_consoleText)
    TextView cdcConsoleText;
    @BindView(R.id.cdc_tv_time)
    TextView cdcTvTime;
    Unbinder unbinder;

    public CdcFragment() {
        // Required empty public constructor
    }

    private int resCount = 1;

    String res = "";
    String rg_t = "text";
    boolean sendDataType = false;

    UsbCdcManager usb_cdc;

    Handler handler = new Handler();
    private int interval = 2000;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (cdcConsoleText.getLineCount() > 500) {
                cdcConsoleText.setText("");
            }
            handler.postDelayed(this, interval);
            cdcSend.performClick();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("cpfalife","cdc --- onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("cpfalife","cdc --- onCreateView");
        View view = inflater.inflate(R.layout.fragment_cdc, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("cpfalife","cdc --- onActivityCreated");
        usb_cdc = new UsbCdcManager(getContext());

        cdcOpen.setOnClickListener(this);
        cdcClose.setOnClickListener(this);
        cdcSend.setOnClickListener(this);
        cdcBtnStartTest.setOnClickListener(this);
        cdcBtnStopTest.setOnClickListener(this);
        cdcBtnStopTest.setEnabled(false);
        /*
        get read data
         */
        usb_cdc.setiRlistener(new UsbCdcManager.IReceiveDataListener() {

            @Override
            public void onReceiveData(String data) {
                Log.d("cpfa", "data activity  = "+data);
                String msg = getTime() +" ResCount:"+resCount+" "+ data + "\n";
                refreshView(msg);
                resCount++;
            }
        });

        cdcRadioBtn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                switch (radioButtonId){
                    case R.id.cdc_send_text:
                        sendDataType = false;
                        break;
                    case R.id.cdc_send_hex:
                        sendDataType = true;
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("cpfalife","cdc --- onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("cpfalife","cdc --- onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("cpfalife","cdc --- onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("cpfalife","cdc --- onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cdc_open:
                // usb hid device connect
                if(usb_cdc.open()){
                    cdcConsoleText.setText("open success");
                    MyLog.i("cdc","open success");
                }
                else {
                    cdcConsoleText.setText("open fail");
                    MyLog.i("cdc","open fail");
                }
                break;
            case R.id.cdc_close:
                //close
                if(usb_cdc.close()){
                    cdcConsoleText.setText("close success");
                    MyLog.i("cdc","close success");
                }
                else {
                    cdcConsoleText.setText("close fail");
                    MyLog.i("cdc","close fail");
                }
                break;
            case R.id.cdc_send:
                //send command
                if (!TextUtils.isEmpty(cdcEtSend.getText().toString())) {
                    usb_cdc.sendData(cdcEtSend.getText().toString(), sendDataType);
                }
                break;
            case R.id.cdc_btn_start_test:
                //start test
                cdcTvTime.setText(getTime());
                if (!TextUtils.isEmpty(cdcEtTimeInterval.getText().toString())){
                    interval = Integer.valueOf(cdcEtTimeInterval.getText().toString());
                }
                if (!TextUtils.isEmpty(cdcEtSend.getText().toString())){
                    handler.postDelayed(runnable,interval);
                    cdcBtnStopTest.setEnabled(true);
                    cdcBtnStartTest.setEnabled(false);
                }else {
                    Toast.makeText(getContext(),"请输入指令", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cdc_btn_stop_test:
                //stop test
                handler.removeCallbacks(runnable);
                cdcBtnStartTest.setEnabled(true);
                Toast.makeText(getContext(),"停止发送指令", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Log.i("cpfalife","cdc --- onDestroyView");
    }

    public String getTime(){
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date =  new Date(System.currentTimeMillis());
        String time   =   formatter.format(date);
        return time;
    }

    void refreshView(String msg){
        cdcConsoleText.append(msg);
        int offset=cdcConsoleText.getLineCount()*cdcConsoleText.getLineHeight();
        if(offset>cdcConsoleText.getHeight()){
            cdcConsoleText.scrollTo(0,offset-cdcConsoleText.getHeight());
        }
    }
}
