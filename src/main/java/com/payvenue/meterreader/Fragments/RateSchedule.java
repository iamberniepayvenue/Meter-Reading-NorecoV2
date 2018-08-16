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

import java.text.DecimalFormat;

import DataBase.DataBaseHandler;

public class RateSchedule extends Fragment {

	View rootView;
	

	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_rate_schedule, container,
				false);

		return rootView;

	}



	@Override
	public void onViewCreated(View view,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		getRateSchedule();

	}

	public void getRateSchedule() {


		DecimalFormat dec;
		dec = new DecimalFormat("#.####");
		dec.setMinimumFractionDigits(4);

		TableLayout table = (TableLayout) rootView.findViewById(R.id.header);

		TableLayout.LayoutParams lastTxtParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.WRAP_CONTENT,
				TableLayout.LayoutParams.WRAP_CONTENT);

		DataBaseHandler db = new DataBaseHandler(getActivity());

		Cursor cursor = db.getRateSchedule(db);

		if (cursor.getCount() > 0) {

			cursor.moveToFirst();
			do {

				TableRow row = new TableRow(getActivity());
				row.setPadding(10, 10, 10, 10);
				row.setGravity(Gravity.CENTER_HORIZONTAL);

				TextView txtratesched = new TextView(getActivity());
				txtratesched.setText(cursor.getString(6));

				TextView txtclass = new TextView(getActivity());
				txtclass.setText(cursor.getString(5));

				TextView txtschedtype = new TextView(getActivity());
				txtschedtype.setText(cursor.getString(7));

				TextView txtratesegment = new TextView(getActivity());
				txtratesegment.setText(cursor.getString(2));

				TextView txtratecomponent = new TextView(getActivity());
				txtratecomponent.setText(cursor.getString(3));


				Float ratevalue = Float.parseFloat(cursor.getString(8));

				TextView txtamount = new TextView(getActivity());
				txtamount.setText(dec.format(ratevalue));

				row.addView(txtratesched);
				row.addView(txtclass);
				row.addView(txtschedtype);
				row.addView(txtratesegment);
				row.addView(txtratecomponent);
				row.addView(txtamount);

				table.addView(row, lastTxtParams);

			} while (cursor.moveToNext());

		}

	}

}
