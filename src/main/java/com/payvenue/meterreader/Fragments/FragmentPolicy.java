package com.payvenue.meterreader.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.payvenue.meterreader.R;

import DataBase.DataBaseHandler;

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

		Cursor cursor = db.getBillingPolicy(db);

		if (cursor.getCount() > 0) {

			cursor.moveToFirst();
			do {

				TableRow row = new TableRow(getActivity());
				row.setPadding(10, 10, 10, 10);
				row.setGravity(Gravity.CENTER_HORIZONTAL);

				TextView txtpolicycode = new TextView(getActivity());
				txtpolicycode.setText(cursor.getString(3));

				TextView txtpolicyname = new TextView(getActivity());
				txtpolicyname.setText(cursor.getString(4));

				TextView txtcustclass = new TextView(getActivity());
				txtcustclass.setText(cursor.getString(6));

				TextView txtminkwh = new TextView(getActivity());
				txtminkwh.setText(cursor.getString(8));

				TextView txtmaxkwh = new TextView(getActivity());
				txtmaxkwh.setText(cursor.getString(9));

				row.addView(txtpolicycode);
				row.addView(txtpolicyname);
				row.addView(txtcustclass);
				row.addView(txtminkwh);
				row.addView(txtmaxkwh);

				table.addView(row, lastTxtParams);

			} while (cursor.moveToNext());

		}

	}

}
