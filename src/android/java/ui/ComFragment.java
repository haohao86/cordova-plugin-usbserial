package ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nlscan.ComAssistant.MyFunc;
import com.nlscan.ComAssistant.SerialHelper;
import com.nlscan.bean.AssistBean;
import com.nlscan.bean.ComBean;
import com.nlscan.ComAssistant.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.SerialPortFinder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import utils.MyLog;

public class ComFragment extends Fragment {

//    EditText editTextRecDisp, editTextLines, editTextCOMA, editTextCOMB, editTextCOMC, editTextCOMD;
//    EditText editTextTimeCOMA, editTextTimeCOMB, editTextTimeCOMC, editTextTimeCOMD;
//    CheckBox checkBoxAutoClear, checkBoxAutoCOMA, checkBoxAutoCOMB, checkBoxAutoCOMC, checkBoxAutoCOMD;
//    Button ButtonClear, ButtonSendCOMA, ButtonSendCOMB, ButtonSendCOMC, ButtonSendCOMD;
//    ToggleButton toggleButtonCOMA, toggleButtonCOMB, toggleButtonCOMC, toggleButtonCOMD;
//    Spinner SpinnerCOMA, SpinnerCOMB, SpinnerCOMC, SpinnerCOMD;
//    Spinner SpinnerBaudRateCOMA, SpinnerBaudRateCOMB, SpinnerBaudRateCOMC, SpinnerBaudRateCOMD;
//    RadioButton radioButtonTxt, radioButtonHex;
    private Context context;
    SerialControl ComA, ComB, ComC, ComD;//4个串口
    DispQueueThread DispQueue;//刷新显示线程
    SerialPortFinder mSerialPortFinder;//串口设备搜索
    AssistBean AssistData;//用于界面数据序列化和反序列化
    int iRecLines = 0;//接收区行数

    @BindView(R.id.editTextRecDisp)
    EditText editTextRecDisp;
    @BindView(R.id.ButtonClear)
    Button ButtonClear;
    @BindView(R.id.radioButtonTxt)
    RadioButton radioButtonTxt;
    @BindView(R.id.radioButtonHex)
    RadioButton radioButtonHex;
    @BindView(R.id.radioGroupOption)
    RadioGroup radioGroupOption;
    @BindView(R.id.editTextLines)
    EditText editTextLines;
    @BindView(R.id.checkBoxAutoClear)
    CheckBox checkBoxAutoClear;
    @BindView(R.id.editTextCOMA)
    EditText editTextCOMA;
    @BindView(R.id.SpinnerCOMA)
    Spinner SpinnerCOMA;
    @BindView(R.id.SpinnerBaudRateCOMA)
    Spinner SpinnerBaudRateCOMA;
    @BindView(R.id.toggleButtonCOMA)
    ToggleButton toggleButtonCOMA;
    @BindView(R.id.editTextTimeCOMA)
    EditText editTextTimeCOMA;
    @BindView(R.id.textView1)
    TextView textView1;
    @BindView(R.id.checkBoxAutoCOMA)
    CheckBox checkBoxAutoCOMA;
    @BindView(R.id.ButtonSendCOMA)
    Button ButtonSendCOMA;
    @BindView(R.id.editTextCOMB)
    EditText editTextCOMB;
    @BindView(R.id.SpinnerCOMB)
    Spinner SpinnerCOMB;
    @BindView(R.id.SpinnerBaudRateCOMB)
    Spinner SpinnerBaudRateCOMB;
    @BindView(R.id.ToggleButtonCOMB)
    ToggleButton toggleButtonCOMB;
    @BindView(R.id.editTextTimeCOMB)
    EditText editTextTimeCOMB;
    @BindView(R.id.TextView01)
    TextView TextView01;
    @BindView(R.id.checkBoxAutoCOMB)
    CheckBox checkBoxAutoCOMB;
    @BindView(R.id.ButtonSendCOMB)
    Button ButtonSendCOMB;
    @BindView(R.id.editTextCOMC)
    EditText editTextCOMC;
    @BindView(R.id.SpinnerCOMC)
    Spinner SpinnerCOMC;
    @BindView(R.id.SpinnerBaudRateCOMC)
    Spinner SpinnerBaudRateCOMC;
    @BindView(R.id.ToggleButtonCOMC)
    ToggleButton toggleButtonCOMC;
    @BindView(R.id.editTextTimeCOMC)
    EditText editTextTimeCOMC;
    @BindView(R.id.TextView02)
    TextView TextView02;
    @BindView(R.id.checkBoxAutoCOMC)
    CheckBox checkBoxAutoCOMC;
    @BindView(R.id.ButtonSendCOMC)
    Button ButtonSendCOMC;
    @BindView(R.id.editTextCOMD)
    EditText editTextCOMD;
    @BindView(R.id.SpinnerCOMD)
    Spinner SpinnerCOMD;
    @BindView(R.id.SpinnerBaudRateCOMD)
    Spinner SpinnerBaudRateCOMD;
    @BindView(R.id.ToggleButtonCOMD)
    ToggleButton toggleButtonCOMD;
    @BindView(R.id.editTextTimeCOMD)
    EditText editTextTimeCOMD;
    @BindView(R.id.TextView03)
    TextView TextView03;
    @BindView(R.id.checkBoxAutoCOMD)
    CheckBox checkBoxAutoCOMD;
    @BindView(R.id.ButtonSendCOMD)
    Button ButtonSendCOMD;
    Unbinder unbinder;

    public ComFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("cpfalife","com --- onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("cpfalife","com --- onCreateView");
        View view = inflater.inflate(R.layout.fragment_com, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("cpfalife","com --- onActivityCreated");
        ComA = new SerialControl();
        ComB = new SerialControl();
        ComC = new SerialControl();
        ComD = new SerialControl();
        DispQueue = new DispQueueThread();
        DispQueue.start();
        AssistData = getAssistData();
        setControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("cpfalife","com --- onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("cpfalife","com --- onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("cpfalife","com --- onStop");
    }


    @Override
    public void onDestroy() {
        Log.i("cpfalife","com --- onDestroy");
        saveAssistData(AssistData);
        CloseComPort(ComA);
        CloseComPort(ComB);
        CloseComPort(ComC);
        CloseComPort(ComD);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CloseComPort(ComA);
        CloseComPort(ComB);
        CloseComPort(ComC);
        CloseComPort(ComD);
//        setContentView(R.layout.main);
        setControls();
    }

    private void setControls() {
//        String appName = getString(R.string.app_name);
//        try {
//            PackageInfo pinfo = getPackageManager().getPackageInfo("ui.CoFragment", PackageManager.GET_CONFIGURATIONS);
//            String versionName = pinfo.versionName;
////			String versionCode = String.valueOf(pinfo.versionCode);
//            setTitle(appName+" V"+versionName);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
//        editTextLines = (EditText) findViewById(R.id.editTextLines);
//        editTextCOMA = (EditText) findViewById(R.id.editTextCOMA);
//        editTextCOMB = (EditText) findViewById(R.id.editTextCOMB);
//        editTextCOMC = (EditText) findViewById(R.id.editTextCOMC);
//        editTextCOMD = (EditText) findViewById(R.id.editTextCOMD);
//        editTextTimeCOMA = (EditText) findViewById(R.id.editTextTimeCOMA);
//        editTextTimeCOMB = (EditText) findViewById(R.id.editTextTimeCOMB);
//        editTextTimeCOMC = (EditText) findViewById(R.id.editTextTimeCOMC);
//        editTextTimeCOMD = (EditText) findViewById(R.id.editTextTimeCOMD);
//
//        checkBoxAutoClear = (CheckBox) findViewById(R.id.checkBoxAutoClear);
//        checkBoxAutoCOMA = (CheckBox) findViewById(R.id.checkBoxAutoCOMA);
//        checkBoxAutoCOMB = (CheckBox) findViewById(R.id.checkBoxAutoCOMB);
//        checkBoxAutoCOMC = (CheckBox) findViewById(R.id.checkBoxAutoCOMC);
//        checkBoxAutoCOMD = (CheckBox) findViewById(R.id.checkBoxAutoCOMD);
//        ButtonClear = (Button) findViewById(R.id.ButtonClear);
//        ButtonSendCOMA = (Button) findViewById(R.id.ButtonSendCOMA);
//        ButtonSendCOMB = (Button) findViewById(R.id.ButtonSendCOMB);
//        ButtonSendCOMC = (Button) findViewById(R.id.ButtonSendCOMC);
//        ButtonSendCOMD = (Button) findViewById(R.id.ButtonSendCOMD);
//        toggleButtonCOMA = (ToggleButton) findViewById(R.id.toggleButtonCOMA);
//        toggleButtonCOMB = (ToggleButton) findViewById(R.id.ToggleButtonCOMB);
//        toggleButtonCOMC = (ToggleButton) findViewById(R.id.ToggleButtonCOMC);
//        toggleButtonCOMD = (ToggleButton) findViewById(R.id.ToggleButtonCOMD);
//        SpinnerCOMA = (Spinner) findViewById(R.id.SpinnerCOMA);
//        SpinnerCOMB = (Spinner) findViewById(R.id.SpinnerCOMB);
//        SpinnerCOMC = (Spinner) findViewById(R.id.SpinnerCOMC);
//        SpinnerCOMD = (Spinner) findViewById(R.id.SpinnerCOMD);
//        SpinnerBaudRateCOMA = (Spinner) findViewById(R.id.SpinnerBaudRateCOMA);
//        SpinnerBaudRateCOMB = (Spinner) findViewById(R.id.SpinnerBaudRateCOMB);
//        SpinnerBaudRateCOMC = (Spinner) findViewById(R.id.SpinnerBaudRateCOMC);
//        SpinnerBaudRateCOMD = (Spinner) findViewById(R.id.SpinnerBaudRateCOMD);
//        radioButtonTxt = (RadioButton) findViewById(R.id.radioButtonTxt);
//        radioButtonHex = (RadioButton) findViewById(R.id.radioButtonHex);

        editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMB.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMC.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMD.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMB.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMC.setOnEditorActionListener(new EditorActionEvent());
        editTextTimeCOMD.setOnEditorActionListener(new EditorActionEvent());
        editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
        editTextCOMB.setOnFocusChangeListener(new FocusChangeEvent());
        editTextCOMC.setOnFocusChangeListener(new FocusChangeEvent());
        editTextCOMD.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMB.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMC.setOnFocusChangeListener(new FocusChangeEvent());
        editTextTimeCOMD.setOnFocusChangeListener(new FocusChangeEvent());

        radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
        radioButtonHex.setOnClickListener(new radioButtonClickEvent());
        ButtonClear.setOnClickListener(new ButtonClickEvent());
        ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());
        ButtonSendCOMB.setOnClickListener(new ButtonClickEvent());
        ButtonSendCOMC.setOnClickListener(new ButtonClickEvent());
        ButtonSendCOMD.setOnClickListener(new ButtonClickEvent());
        toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        toggleButtonCOMB.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        toggleButtonCOMC.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        toggleButtonCOMD.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
        checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());
        checkBoxAutoCOMB.setOnCheckedChangeListener(new CheckBoxChangeEvent());
        checkBoxAutoCOMC.setOnCheckedChangeListener(new CheckBoxChangeEvent());
        checkBoxAutoCOMD.setOnCheckedChangeListener(new CheckBoxChangeEvent());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.baudrates_value, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerBaudRateCOMA.setAdapter(adapter);
        SpinnerBaudRateCOMB.setAdapter(adapter);
        SpinnerBaudRateCOMC.setAdapter(adapter);
        SpinnerBaudRateCOMD.setAdapter(adapter);
        SpinnerBaudRateCOMA.setSelection(12);
        SpinnerBaudRateCOMB.setSelection(12);
        SpinnerBaudRateCOMC.setSelection(12);
        SpinnerBaudRateCOMD.setSelection(12);

        mSerialPortFinder = new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<String>();
        for (int i = 0; i < entryValues.length; i++) {
            allDevices.add(entryValues[i]);
        }
        ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, allDevices);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerCOMA.setAdapter(aspnDevices);
        SpinnerCOMB.setAdapter(aspnDevices);
        SpinnerCOMC.setAdapter(aspnDevices);
        SpinnerCOMD.setAdapter(aspnDevices);
        if (allDevices.size() > 0) {
            SpinnerCOMA.setSelection(0);
        }
        if (allDevices.size() > 1) {
            SpinnerCOMB.setSelection(1);
        }
        if (allDevices.size() > 2) {
            SpinnerCOMC.setSelection(2);
        }
        if (allDevices.size() > 3) {
            SpinnerCOMD.setSelection(3);
        }
        SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerCOMB.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerCOMC.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerCOMD.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerBaudRateCOMB.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerBaudRateCOMC.setOnItemSelectedListener(new ItemSelectedEvent());
        SpinnerBaudRateCOMD.setOnItemSelectedListener(new ItemSelectedEvent());
        DispAssistData(AssistData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Log.i("cpfalife","com --- onDestroyView");
    }

    //----------------------------------------------------串口号或波特率变化时，关闭打开的串口
    class ItemSelectedEvent implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if ((arg0 == SpinnerCOMA) || (arg0 == SpinnerBaudRateCOMA)) {
                CloseComPort(ComA);
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMA.setChecked(false);
            } else if ((arg0 == SpinnerCOMB) || (arg0 == SpinnerBaudRateCOMB)) {
                CloseComPort(ComB);
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMB.setChecked(false);
            } else if ((arg0 == SpinnerCOMC) || (arg0 == SpinnerBaudRateCOMC)) {
                CloseComPort(ComC);
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMC.setChecked(false);
            } else if ((arg0 == SpinnerCOMD) || (arg0 == SpinnerBaudRateCOMD)) {
                CloseComPort(ComD);
                checkBoxAutoCOMA.setChecked(false);
                toggleButtonCOMD.setChecked(false);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }

    //----------------------------------------------------编辑框焦点转移事件
    class FocusChangeEvent implements EditText.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v == editTextCOMA) {
                setSendData(editTextCOMA);
            } else if (v == editTextCOMB) {
                setSendData(editTextCOMB);
            } else if (v == editTextCOMC) {
                setSendData(editTextCOMC);
            } else if (v == editTextCOMD) {
                setSendData(editTextCOMD);
            } else if (v == editTextTimeCOMA) {
                setDelayTime(editTextTimeCOMA);
            } else if (v == editTextTimeCOMB) {
                setDelayTime(editTextTimeCOMB);
            } else if (v == editTextTimeCOMC) {
                setDelayTime(editTextTimeCOMC);
            } else if (v == editTextTimeCOMD) {
                setDelayTime(editTextTimeCOMD);
            }
        }
    }

    //----------------------------------------------------编辑框完成事件
    class EditorActionEvent implements EditText.OnEditorActionListener {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (v == editTextCOMA) {
                setSendData(editTextCOMA);
            } else if (v == editTextCOMB) {
                setSendData(editTextCOMB);
            } else if (v == editTextCOMC) {
                setSendData(editTextCOMC);
            } else if (v == editTextCOMD) {
                setSendData(editTextCOMD);
            } else if (v == editTextTimeCOMA) {
                setDelayTime(editTextTimeCOMA);
            } else if (v == editTextTimeCOMB) {
                setDelayTime(editTextTimeCOMB);
            } else if (v == editTextTimeCOMC) {
                setDelayTime(editTextTimeCOMC);
            } else if (v == editTextTimeCOMD) {
                setDelayTime(editTextTimeCOMD);
            }
            return false;
        }
    }

    //----------------------------------------------------Txt、Hex模式选择
    class radioButtonClickEvent implements RadioButton.OnClickListener {
        public void onClick(View v) {
            if (v == radioButtonTxt) {
                KeyListener TxtkeyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, false);
                editTextCOMA.setKeyListener(TxtkeyListener);
                editTextCOMB.setKeyListener(TxtkeyListener);
                editTextCOMC.setKeyListener(TxtkeyListener);
                editTextCOMD.setKeyListener(TxtkeyListener);
                AssistData.setTxtMode(true);
            } else if (v == radioButtonHex) {
                KeyListener HexkeyListener = new NumberKeyListener() {
                    public int getInputType() {
                        return InputType.TYPE_CLASS_TEXT;
                    }

                    @Override
                    protected char[] getAcceptedChars() {
                        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
                    }
                };
                editTextCOMA.setKeyListener(HexkeyListener);
                editTextCOMB.setKeyListener(HexkeyListener);
                editTextCOMC.setKeyListener(HexkeyListener);
                editTextCOMD.setKeyListener(HexkeyListener);
                AssistData.setTxtMode(false);
            }
            editTextCOMA.setText(AssistData.getSendA());
            editTextCOMB.setText(AssistData.getSendB());
            editTextCOMC.setText(AssistData.getSendC());
            editTextCOMD.setText(AssistData.getSendD());
            setSendData(editTextCOMA);
            setSendData(editTextCOMB);
            setSendData(editTextCOMC);
            setSendData(editTextCOMD);
        }
    }

    //----------------------------------------------------自动发送
    class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == checkBoxAutoCOMA) {
                if (!toggleButtonCOMA.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(ComA, editTextCOMA.getText().toString());
                SetAutoSend(ComA, isChecked);
            } else if (buttonView == checkBoxAutoCOMB) {
                if (!toggleButtonCOMB.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(ComB, editTextCOMB.getText().toString());
                SetAutoSend(ComB, isChecked);
            } else if (buttonView == checkBoxAutoCOMC) {
                if (!toggleButtonCOMC.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(ComC, editTextCOMC.getText().toString());
                SetAutoSend(ComC, isChecked);
            } else if (buttonView == checkBoxAutoCOMD) {
                if (!toggleButtonCOMD.isChecked() && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                SetLoopData(ComD, editTextCOMD.getText().toString());
                SetAutoSend(ComD, isChecked);
            }
        }
    }

    //----------------------------------------------------清除按钮、发送按钮
    class ButtonClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == ButtonClear) {
                editTextRecDisp.setText("");
            } else if (v == ButtonSendCOMA) {
                sendPortData(ComA, editTextCOMA.getText().toString());
            } else if (v == ButtonSendCOMB) {
                sendPortData(ComB, editTextCOMB.getText().toString());
            } else if (v == ButtonSendCOMC) {
                sendPortData(ComC, editTextCOMC.getText().toString());
            } else if (v == ButtonSendCOMD) {
                sendPortData(ComD, editTextCOMD.getText().toString());
            }
        }
    }

    //----------------------------------------------------打开关闭串口
    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == toggleButtonCOMA) {
                if (isChecked) {
                    if (toggleButtonCOMB.isChecked() && SpinnerCOMA.getSelectedItemPosition() == SpinnerCOMB.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMA.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMC.isChecked() && SpinnerCOMA.getSelectedItemPosition() == SpinnerCOMC.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMA.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMD.isChecked() && SpinnerCOMA.getSelectedItemPosition() == SpinnerCOMD.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMA.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else {
//						ComA=new SerialControl("/dev/s3c2410_serial0", "9600");
                        ComA.setPort(SpinnerCOMA.getSelectedItem().toString());
                        ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem().toString());
                        OpenComPort(ComA);
                    }
                } else {
                    CloseComPort(ComA);
                    checkBoxAutoCOMA.setChecked(false);
                }
            } else if (buttonView == toggleButtonCOMB) {
                if (isChecked) {
                    if (toggleButtonCOMA.isChecked() && SpinnerCOMB.getSelectedItemPosition() == SpinnerCOMA.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMB.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMC.isChecked() && SpinnerCOMB.getSelectedItemPosition() == SpinnerCOMC.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMB.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMD.isChecked() && SpinnerCOMB.getSelectedItemPosition() == SpinnerCOMD.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMB.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else {
//						ComB=new SerialControl("/dev/s3c2410_serial1", "9600");
                        ComB.setPort(SpinnerCOMB.getSelectedItem().toString());
                        ComB.setBaudRate(SpinnerBaudRateCOMB.getSelectedItem().toString());
                        OpenComPort(ComB);
                    }
                } else {
                    CloseComPort(ComB);
                    checkBoxAutoCOMB.setChecked(false);
                }
            } else if (buttonView == toggleButtonCOMC) {
                if (isChecked) {
                    if (toggleButtonCOMA.isChecked() && SpinnerCOMC.getSelectedItemPosition() == SpinnerCOMA.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMC.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMB.isChecked() && SpinnerCOMC.getSelectedItemPosition() == SpinnerCOMB.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMC.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMD.isChecked() && SpinnerCOMC.getSelectedItemPosition() == SpinnerCOMD.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMC.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else {
                        //					ComC=new SerialControl("/dev/s3c2410_serial2", "9600");
                        ComC.setPort(SpinnerCOMC.getSelectedItem().toString());
                        ComC.setBaudRate(SpinnerBaudRateCOMC.getSelectedItem().toString());
                        OpenComPort(ComC);
                    }
                } else {
                    CloseComPort(ComC);
                    checkBoxAutoCOMC.setChecked(false);
                }
            } else if (buttonView == toggleButtonCOMD) {
                if (isChecked) {
                    if (toggleButtonCOMA.isChecked() && SpinnerCOMD.getSelectedItemPosition() == SpinnerCOMA.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMD.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMB.isChecked() && SpinnerCOMD.getSelectedItemPosition() == SpinnerCOMB.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMD.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else if (toggleButtonCOMC.isChecked() && SpinnerCOMD.getSelectedItemPosition() == SpinnerCOMC.getSelectedItemPosition()) {
                        ShowMessage("串口" + SpinnerCOMD.getSelectedItem().toString() + "已打开");
                        buttonView.setChecked(false);
                    } else {
                        //					ComD=new SerialControl("/dev/s3c2410_serial3", "9600");
                        ComD.setPort(SpinnerCOMD.getSelectedItem().toString());
                        ComD.setBaudRate(SpinnerBaudRateCOMD.getSelectedItem().toString());
                        OpenComPort(ComD);
                    }
                } else {
                    CloseComPort(ComD);
                    checkBoxAutoCOMD.setChecked(false);
                }
            }
        }
    }

    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper {

        //		public SerialControl(String sPort, String sBaudRate){
//			super(sPort, sBaudRate);
//		}
        public SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
            DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
            /*
			runOnUiThread(new Runnable()//直接刷新显示
			{
				public void run()
				{
					DispRecData(ComRecData);
				}
			});*/
        }
    }

    //----------------------------------------------------刷新显示线程
    private class DispQueueThread extends Thread{
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                final ComBean ComData;
                while((ComData=QueueList.poll())!=null)
                {
                    ((Activity)context).runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            try {
                                DispRecData(ComData);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }

                        };
                    });
                    try
                    {
                        Thread.sleep(100);//显示性能高的话，可以把此数值调小。
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData){
            QueueList.add(ComData);
        }
    }

    //----------------------------------------------------刷新界面数据
    private void DispAssistData(AssistBean AssistData) {
        editTextCOMA.setText(AssistData.getSendA());
        editTextCOMB.setText(AssistData.getSendB());
        editTextCOMC.setText(AssistData.getSendC());
        editTextCOMD.setText(AssistData.getSendD());
        setSendData(editTextCOMA);
        setSendData(editTextCOMB);
        setSendData(editTextCOMC);
        setSendData(editTextCOMD);
        if (AssistData.isTxt()) {
            radioButtonTxt.setChecked(true);
        } else {
            radioButtonHex.setChecked(true);
        }
        editTextTimeCOMA.setText(AssistData.sTimeA);
        editTextTimeCOMB.setText(AssistData.sTimeB);
        editTextTimeCOMC.setText(AssistData.sTimeC);
        editTextTimeCOMD.setText(AssistData.sTimeD);
        setDelayTime(editTextTimeCOMA);
        setDelayTime(editTextTimeCOMB);
        setDelayTime(editTextTimeCOMC);
        setDelayTime(editTextTimeCOMD);
    }

    //----------------------------------------------------保存、获取界面数据
    private void saveAssistData(AssistBean AssistData) {
        try {
            AssistData.sTimeA = editTextTimeCOMA.getText().toString();
            AssistData.sTimeB = editTextTimeCOMB.getText().toString();
            AssistData.sTimeC = editTextTimeCOMC.getText().toString();
            AssistData.sTimeD = editTextTimeCOMD.getText().toString();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        SharedPreferences msharedPreferences = getActivity().getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(AssistData);
            String sBase64 = new String(Base64.encode(baos.toByteArray(), 0));
            SharedPreferences.Editor editor = msharedPreferences.edit();
            editor.putString("AssistData", sBase64);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
            MyLog.e("ERROR", e);
        }
    }

    //----------------------------------------------------
    private AssistBean getAssistData() {
        SharedPreferences msharedPreferences = getActivity().getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
        AssistBean AssistData = new AssistBean();
        try {
            String personBase64 = msharedPreferences.getString("AssistData", "");
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            AssistData = (AssistBean) ois.readObject();
            return AssistData;
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.e("ERROR", e);
        }
        return AssistData;
    }

    //----------------------------------------------------设置自动发送延时
    private void setDelayTime(TextView v) {
        if (v == editTextTimeCOMA) {
            AssistData.sTimeA = v.getText().toString();
            SetiDelayTime(ComA, v.getText().toString());
        } else if (v == editTextTimeCOMB) {
            AssistData.sTimeB = v.getText().toString();
            SetiDelayTime(ComB, v.getText().toString());
        } else if (v == editTextTimeCOMC) {
            AssistData.sTimeC = v.getText().toString();
            SetiDelayTime(ComC, v.getText().toString());
        } else if (v == editTextTimeCOMD) {
            AssistData.sTimeD = v.getText().toString();
            SetiDelayTime(ComD, v.getText().toString());
        }
    }

    //----------------------------------------------------设置自动发送数据
    private void setSendData(TextView v) {
        if (v == editTextCOMA) {
            AssistData.setSendA(v.getText().toString());
            SetLoopData(ComA, v.getText().toString());
        } else if (v == editTextCOMB) {
            AssistData.setSendB(v.getText().toString());
            SetLoopData(ComB, v.getText().toString());
        } else if (v == editTextCOMC) {
            AssistData.setSendC(v.getText().toString());
            SetLoopData(ComC, v.getText().toString());
        } else if (v == editTextCOMD) {
            AssistData.setSendD(v.getText().toString());
            SetLoopData(ComD, v.getText().toString());
        }
    }

    //----------------------------------------------------设置自动发送延时
    private void SetiDelayTime(SerialHelper ComPort, String sTime) {
        ComPort.setiDelay(Integer.parseInt(sTime));
    }

    //----------------------------------------------------设置自动发送数据
    private void SetLoopData(SerialHelper ComPort, String sLoopData) {
        if (radioButtonTxt.isChecked()) {
            ComPort.setTxtLoopData(sLoopData);
        } else if (radioButtonHex.isChecked()) {
            ComPort.setHexLoopData(sLoopData);
        }
    }

    //----------------------------------------------------显示接收数据
    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        sMsg.append(ComRecData.sRecTime);
        sMsg.append("[");
        sMsg.append(ComRecData.sComPort);
        sMsg.append("]");
        if (radioButtonTxt.isChecked()) {
            sMsg.append("[Txt] ");
            sMsg.append(new String(ComRecData.bRec));
        } else if (radioButtonHex.isChecked()) {
            sMsg.append("[Hex] ");
            sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
        }
        sMsg.append("\r\n");
        editTextRecDisp.append(sMsg);
        iRecLines++;
        editTextLines.setText(String.valueOf(iRecLines));
        if ((iRecLines > 500) && (checkBoxAutoClear.isChecked()))//达到500项自动清除
        {
            editTextRecDisp.setText("");
            editTextLines.setText("0");
            iRecLines = 0;
        }
    }

    //----------------------------------------------------设置自动发送模式开关
    private void SetAutoSend(SerialHelper ComPort, boolean isAutoSend) {
        if (isAutoSend) {
            ComPort.startSend();
        } else {
            ComPort.stopSend();
        }
    }

    //----------------------------------------------------串口发送
    private void sendPortData(SerialHelper ComPort, String sOut) {
        if (ComPort != null && ComPort.isOpen()) {
            if (radioButtonTxt.isChecked()) {
                ComPort.sendTxt(sOut);
            } else if (radioButtonHex.isChecked()) {
                ComPort.sendHex(sOut);
            }
        }
    }

    //----------------------------------------------------关闭串口
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            ShowMessage("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ShowMessage("打开串口失败:参数错误!");
        }
    }

    //------------------------------------------显示消息
    private void ShowMessage(String sMsg) {
        Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show();
        MyLog.i("INFO", sMsg);
    }
}
