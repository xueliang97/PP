package com.hdu.pp.ui.my;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdu.libnavannotation.FragmentDestination;
import com.hdu.pp.R;

/**
 * A simple {@link Fragment} subclass.
 */
@FragmentDestination(pageUrl = "main/tabs/my",asStarter = false,needLogin = true)
public class MyFragment extends Fragment {

    private static final String TAG = "MyFragment";
    public MyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.e(TAG, "onCreateView: " );
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

}
