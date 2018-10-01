package com.payvenue.meterreader.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import java.util.ArrayList;

import DataBase.DataBaseHandler;
import Model.Policies;

public class FragmentPolicy extends Fragment {

	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_billing_policy,
				container, false);

		// displayPolicy();

		return rootView;

	}

	@Override
	public void onViewCreated(View view,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		displayPolicy();
	}

	public void displayPolicy() {

		TableLayout table = (TableLayout) rootView.findViewById(R.id.tblPolicy);

		TableLayout.LayoutParams lastTxtParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.WRAP_CONTENT,
				TableLayout.LayoutParams.WRAP_CONTENT);

		DataBaseHandler db = new DataBaseHandler(getActivity());
		ArrayList<Policies> policiesArrayList = new ArrayList<>();
		//Cursor cursor = db.getBillingPolicy(db,MainActivity.selectedAccount.getAccountClassification());
		policiesArrayList = db.getBillingPolicy(db,MainActivity.selectedAccount.getAccountClassification());
		if (policiesArrayList.size() > 0) {
			for(Policies p: policiesArrayList) {
				TableRow row = new TableRow(getActivity());
				row.setPadding(10, 10, 10, 10);
				row.setGravity(Gravity.CENTER_HORIZONTAL);

				TextView txtpolicycode = new TextView(getActivity());
				txtpolicycode.setText(p.getPolicyCode());

				//TextView txtpolicyname = new TextView(getActivity());
				//txtpolicyname.setText(cursor.getString(4));

				TextView txtcustclass = new TextView(getActivity());
				txtcustclass.setText(p.getCustomerClass());

				TextView txtminkwh = new TextView(getActivity());
				txtminkwh.setText(p.getMinkWh());

				TextView txtmaxkwh = new TextView(getActivity());
				txtmaxkwh.setText(p.getMaxkWh());

				row.addView(txtpolicycode);
				//row.addView(txtpolicyname);
				row.addView(txtcustclass);
				row.addView(txtminkwh);
				row.addView(txtmaxkwh);

				table.addView(row, lastTxtParams);
			}
		}

	}

}
