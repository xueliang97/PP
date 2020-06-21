package com.hdu.pp.ui.sofa;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hdu.libnavannotation.FragmentDestination;
import com.hdu.pp.databinding.FragmentSofaBinding;
import com.hdu.pp.model.SofaTab;
import com.hdu.pp.ui.home.HomeFragment;
import com.hdu.pp.utils.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
@FragmentDestination(pageUrl = "main/tabs/sofa",asStarter = false)
public class SofaFragment extends Fragment {
    private static final String TAG = "SofaFragment";

    private FragmentSofaBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ArrayList<SofaTab.Tabs> tabs;
    private SofaTab tabConfig;

    private Map<Integer,Fragment> mFragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;

    public SofaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for(SofaTab.Tabs tab:tabConfig.tabs){
            if (tab.enable) {
                tabs.add(tab);
            }
        }

        //限制页面预加载
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(),this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {

                Fragment fragment = mFragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                    mFragmentMap.put(position, fragment);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });


        //viewPager2不再使用tab.setUpWithViewPager()，使用TabLayoutMediator
        mediator = new TabLayoutMediator(tabLayout, viewPager, false, (tab, position) -> {
            tab.setCustomView(makeTabView(position));
        });
        mediator.attach();

        viewPager.registerOnPageChangeCallback(mPageChangeCallback);
        //切换到默认选择项,等待初始化完成之后才有效
        viewPager.post(() -> viewPager.setCurrentItem(tabConfig.select));

    }

    ViewPager2.OnPageChangeCallback mPageChangeCallback  = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {

                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private TextView makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    private Fragment getTabFragment(int position) {

        return HomeFragment.newInstance(tabs.get(position).toString());
    }

    private SofaTab getTabConfig() {
        return AppConfig.getsSofaTab();
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroy();
    }
}
