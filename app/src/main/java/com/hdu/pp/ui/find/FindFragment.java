package com.hdu.pp.ui.find;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdu.libnavannotation.FragmentDestination;
import com.hdu.pp.R;
import com.hdu.pp.model.SofaTab;
import com.hdu.pp.ui.sofa.SofaFragment;
import com.hdu.pp.utils.AppConfig;

/**
 * A simple {@link Fragment} subclass.
 */
@FragmentDestination(pageUrl = "main/tabs/find",asStarter = false)
public class FindFragment extends SofaFragment {
    private static final String TAG = "FindFragment";

    //创建对应标签的Fragment页面
    @Override
    public Fragment getTabFragment(int position) {
        TagListFragment fragment = TagListFragment.newInstance(getTabConfig().tabs.get(position).tag);
        return fragment;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            ViewModelProviders.of(childFragment).get(TagListViewModel.class)
                    .getSwitchTabLiveData().observe(this,
                    object -> viewPager.setCurrentItem(1));
        }
    }

    //创建顶部标签的配置数据
    @Override
    public SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }
}
