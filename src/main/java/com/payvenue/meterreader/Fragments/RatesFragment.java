package com.payvenue.meterreader.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.payvenue.meterreader.R;

public class RatesFragment extends Fragment {

	View rootView;

	String coopid;
	String port;
	String host;

	int SaveCount;
	int rateschedulecount;

	private FragmentTabHost mTabHost;

	ProgressDialog mDialog;

	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		rootView = inflater.inflate(R.layout.fragment_rates, container, false);

		mDialog = new ProgressDialog(this.getActivity());
		mDialog.setCancelable(true);
		mDialog.setMessage("Synching Rate Management. Please wait...");



		mTabHost = new FragmentTabHost(getActivity());
		mTabHost.setup(this.getActivity(), getChildFragmentManager(),
				R.layout.fragment_rates);


		mTabHost.addTab(
				mTabHost.newTabSpec("4").setIndicator("Rate Schedule",
						getResources().getDrawable(R.drawable.ic_drawer)),
				com.payvenue.meterreader.Fragments.RateSchedule.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("5").setIndicator("Rate Policy",
						getResources().getDrawable(R.drawable.ic_drawer)),
				FragmentPolicy.class, null);


		return mTabHost;

	}





	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.rates, menu);
	}

}
