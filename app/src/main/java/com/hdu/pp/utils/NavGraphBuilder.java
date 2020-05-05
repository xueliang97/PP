package com.hdu.pp.utils;

import android.content.ComponentName;

import com.hdu.libcommon.AppGlobals;
import com.hdu.pp.FixFragmentNavigator;
import com.hdu.pp.model.Destination;

import java.util.HashMap;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

public class NavGraphBuilder {//根据实体类创建NavGraph

    public static void build(FragmentManager childFragmentManager,NavController controller, FragmentActivity activity, int containerId){
        NavigatorProvider provider = controller.getNavigatorProvider();

     //   FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(activity,childFragmentManager,containerId);
        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);
        provider.addNavigator(fragmentNavigator);
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig = AppConfig.getsDestConfig();

        for(Destination value : destConfig.values()){
            if (value.isFragment){
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setId(value.id);
                destination.setClassName(value.className);
                destination.addDeepLink(value.pageUrl);
                navGraph.addDestination(destination);

            }else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.id);
                destination.setComponentName(new ComponentName(AppGlobals.getApplication().getPackageName(),value.className));
                destination.addDeepLink(value.pageUrl);
                navGraph.addDestination(destination);
            }

            if (value.asStarter){
                navGraph.setStartDestination(value.id);
            }
        }
        controller.setGraph(navGraph);




    }
}
