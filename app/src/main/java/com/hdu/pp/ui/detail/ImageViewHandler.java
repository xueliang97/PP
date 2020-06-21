package com.hdu.pp.ui.detail;

import com.hdu.pp.R;
import com.hdu.pp.databinding.ActivityFeedDetailTypeImageBinding;
import com.hdu.pp.model.Feed;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

/**
 * 图文详情页处理逻辑
 */
public class ImageViewHandler extends ViewHandler{

    ActivityFeedDetailTypeImageBinding mImageBinding;
    public ImageViewHandler(FragmentActivity activity) {
        super(activity);

        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);

    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mImageBinding.setFeed(feed);
    }
}
