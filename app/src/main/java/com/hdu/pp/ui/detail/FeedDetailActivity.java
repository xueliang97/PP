package com.hdu.pp.ui.detail;

import android.os.Bundle;
import android.os.Parcelable;

import com.hdu.pp.model.Feed;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FeedDetailActivity  extends AppCompatActivity {

    private static final String KEY_FEED = "key_feed";
    public static final String KEY_CATEGORY = "key_category";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Feed feed = getIntent().getParcelableExtra(KEY_FEED);
        if (feed==null){
            finish();
            return;
        }

        ViewHandler viewHandler = null;
        if(feed.itemType==Feed.TYPE_IMAGE){
            viewHandler = new ImageViewHandler(this);
        }else {
            viewHandler = new VideoViewHandler(this);
        }

        viewHandler.bindInitData(feed);
    }
}
