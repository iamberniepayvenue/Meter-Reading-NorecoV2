package com.payvenue.meterreader.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payvenue.meterreader.Interface.MyDialogInterface;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class MyDialogFragment extends AppCompatDialogFragment {
    public static MyDialogInterface mListener;
    private Button btnOK;
    private EditText txtPrevReading;
    private EditText txtPresReading;
    InputMethodManager imm;




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_my_dialog,null);

        try {
            imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        } catch (Exception e) {
            // TODO: handle exception
        }

        txtPrevReading = v.findViewById(R.id.txtExportPrevReading);
        txtPresReading = v.findViewById(R.id.txtExportPressReading);


        txtPrevReading.setText(MainActivity.selectedAccount.getExportPreviousReading() + " kWh");
        txtPresReading.requestFocus();
        txtPresReading.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int i, KeyEvent event) {
                hideKeyboard();
                if(v.getId() == R.id.txtExportPressReading) {
                    if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_NEXT) {
                        validate();
                    }
                }
                return false;
            }
        });
        btnOK = v.findViewById(R.id.btnOk);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }

    public void hideKeyboard() {
        try {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        } catch (Exception e) {

        }
    }

    public void validate() {
        if(txtPrevReading.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(getActivity(),"Invalid Prev. RDG",Toast.LENGTH_SHORT).show();
            return;
        }

        if(txtPresReading.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(getActivity(),"Invalid Pres. RDG",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!txtPrevReading.getText().toString().equalsIgnoreCase("") &&
                !txtPresReading.getText().toString().equalsIgnoreCase("")) {
            MainActivity.selectedAccount.setExportReading(txtPresReading.getText().toString());
            mListener.onMyDialogDismiss(0);
            dismiss();
        }
    }
}
