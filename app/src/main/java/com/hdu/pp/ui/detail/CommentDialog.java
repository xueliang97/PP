package com.hdu.pp.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hdu.libcommon.dialog.LoadingDialog;
import com.hdu.libcommon.global.AppGlobals;
import com.hdu.libcommon.utils.FileUploadManager;
import com.hdu.libcommon.utils.FileUtils;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.libnetwork.JsonCallback;
import com.hdu.pp.R;
import com.hdu.pp.databinding.LayoutCommentDialogBinding;
import com.hdu.pp.login.MyUserManager;
import com.hdu.pp.model.Comment;
import com.hdu.pp.ui.publish.CaptureActivity;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

public class CommentDialog extends DialogFragment implements View.OnClickListener {
    private LayoutCommentDialogBinding mBinding;
    private long itemId;//唤起对话框时传入
    private commentAddListener mListener;
    private static final String KEY_ITEM_ID = "key_item_id";
    private String filePath;
    private int width,height;
    private boolean isVideo;
    private LoadingDialog loadingDialog;
    private String coverUrl;
    private String fileUrl;

    public static CommentDialog newInstance(long itemId) {
        
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutCommentDialogBinding.inflate(inflater,container,false);

        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        this.itemId = getArguments().getLong(KEY_ITEM_ID);
        return mBinding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.comment_send) {
            publishComment();
        } else if (v.getId() == R.id.comment_video) {
            CaptureActivity.startActivityForResult(getActivity());
        } else if (v.getId() == R.id.comment_delete) {
            filePath = null;
            isVideo = false;
            width = 0;
            height = 0;
            mBinding.commentCover.setImageDrawable(null);
            mBinding.commentExtLayout.setVisibility(View.GONE);

            mBinding.commentVideo.setEnabled(true);
            mBinding.commentVideo.setAlpha(255);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(filePath);
                if (isVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(80);
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }

        if (isVideo && !TextUtils.isEmpty(filePath)) {
            FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFile(coverPath, filePath);
                }
            });
        } else if (!TextUtils.isEmpty(filePath)) {
            uploadFile(null, filePath);
        } else {
            publish();
        }
    }

    private void uploadFile(String coverPath, String filePath) { //文件上传到阿里云
        //多文件上传保证同步
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if(!TextUtils.isEmpty(coverPath)){//上传封面
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(()->{
                int remain = count.decrementAndGet();
                coverUrl = FileUploadManager.upload(coverPath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(()->{ //上传视频或图片
            int remain = count.decrementAndGet();
            fileUrl = FileUploadManager.upload(filePath);
            if (remain<=0){//所有文件都上传结束
                if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath)&& !TextUtils.isEmpty(coverUrl)) {
                    publish();
                } else {
                    dismissLoadingDialog();
                    showToast(getString(R.string.file_upload_failed));
                }
            }
        });

    }

    private void showLoadingDialog(){
        if (loadingDialog==null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.setLoadingText(getString(R.string.upload_text));
        loadingDialog.show();
    }

    private void publish() {//真正的发布评论
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", MyUserManager.get().getUserId())
                .addParam("itemId", itemId) //itemId唤起评论对话框传入
                .addParam("commentText", commentText)
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .addParam("video_url", isVideo ? fileUrl : null)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败:" + response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    private void dismissLoadingDialog() {
        if (loadingDialog!=null)
            loadingDialog.dismiss();
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if(mListener!=null) {
                    mListener.onAddComment(body);
                }
                dismiss();
            }
        });
    }

    public interface commentAddListener{
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener){
        mListener = listener;

    }

    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }
}
