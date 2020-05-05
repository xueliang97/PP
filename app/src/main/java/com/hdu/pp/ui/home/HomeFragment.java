package com.hdu.pp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hdu.libnavannotation.FragmentDestination;
import com.hdu.pp.R;

@FragmentDestination(pageUrl = "main/tabs/home",asStarter = true)
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: " );
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }
}