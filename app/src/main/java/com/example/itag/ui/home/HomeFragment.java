package com.example.itag.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.itag.MyApp;
import com.example.itag.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private Button button_home_connect,button_home_mysql;
    private TextView textView_home_state,textView_home_mysql;
    private boolean isOpen;
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://ip/tag?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
    static final String USER = "";
    static final String PASS = "";

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        com.example.itag.MyApp.driver = new CH34xUARTDriver((UsbManager) getActivity().getSystemService(Context.USB_SERVICE), getContext(), ACTION_USB_PERMISSION);
        button_home_connect=view.findViewById(R.id.button_home_connect);
        textView_home_state=view.findViewById(R.id.textView_home_state);
        button_home_mysql=view.findViewById(R.id.button_home_mysql);
        textView_home_mysql=view.findViewById(R.id.textView_home_mysql);

        button_home_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifUSB();
                if(openUSB()){
                    textView_home_state.setText("??????:?????????");
                }
            }
        });

        button_home_mysql.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(runnable).start();
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

    private void ifUSB(){
        if (!com.example.itag.MyApp.driver.UsbFeatureSupported())// ????????????????????????USB HOST
        {
            Dialog dialog = new AlertDialog.Builder(getContext()).setTitle("??????").setMessage("?????????????????????USB HOST?????????????????????????????????").setPositiveButton
                    ("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            System.exit(0);
                        }
                    }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private boolean openUSB(){
        if (!isOpen) {
            int retVal = MyApp.driver.ResumeUsbList();  // ResumeUsbList??????????????????CH34X??????????????????????????????
            if (retVal == -1)
            {
                com.example.itag.MyApp.driver.CloseDevice();
                return false;
            }
            else if (retVal == 0){
                if (!com.example.itag.MyApp.driver.UartInit()) {   //????????????????????????????????????
                    com.example.itag.MyApp.driver.CloseDevice();
                    return false;
                }
                else {
                    isOpen = true;
                    button_home_connect.setText("????????????");

                    return true;
                }
            }
            else {
                com.example.itag.MyApp.driver.CloseDevice();
                return false;
            }
        }
        else {
            com.example.itag.MyApp.driver.CloseDevice();
            button_home_connect.setText("????????????");
            textView_home_state.setText("??????:?????????");
            isOpen = false;
            return false;
        }
    }
    Runnable runnable = new Runnable() {

        private Connection conn = null;
        @Override
        public void run() {
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL,USER,PASS);
            } catch (SQLException | ClassNotFoundException e) {
                textView_home_mysql.setText(e.toString());
            }
            try {
                textView_home_mysql.setText("??????:?????????");
            } catch (Exception e) {
                textView_home_mysql.setText(e.toString());
            }
        }
    };

}