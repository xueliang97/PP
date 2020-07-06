package com.hdu.pp.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hdu.libcommon.dialog.LoadingDialog;
import com.hdu.libcommon.utils.FileUtils;
import com.hdu.libnavannotation.ActivityDestination;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.libnetwork.JsonCallback;
import com.hdu.pp.R;
import com.hdu.pp.databinding.ActivityLayoutPublishBinding;
import com.hdu.pp.login.MyUserManager;
import com.hdu.pp.model.Feed;
import com.hdu.pp.model.TagList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActivityDestination(pageUrl = "main/tabs/publish",needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLayoutPublishBinding mBinding;
    private int width, height;
    private String filePath, coverFilePath;
    private boolean isVideo;
    private UUID coverUploadUUID;
    private UUID fileUploadUUID;
    private String coverUploadUrl;
    private String fileUploadUrl;
    private TagList mTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_publish);

        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionAddFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_publish:
                publish();
                break;
            case R.id.action_add_tag:
                TagBottomSheetDialogFragment fragment = new TagBottomSheetDialogFragment();
                fragment.setOnTagItemSelectedListener(new TagBottomSheetDialogFragment.OnTagItemSelectedListener() {
                    @Override
                    public void onTagItemSelected(TagList tagList) {
                        mTagList = tagList;
                        mBinding.actionAddTag.setText(tagList.title);
                    }
                });
                fragment.show(getSupportFragmentManager(), "tag_dialog");
                break;
            case R.id.action_add_file:
                CaptureActivity.startActivityForResult(this);
                break;
            case R.id.action_delete_file:
                mBinding.actionAddFile.setVisibility(View.VISIBLE);
                mBinding.fileContainer.setVisibility(View.GONE);
                mBinding.cover.setImageDrawable(null);
                filePath = null;
                width = 0;
                height = 0;
                isVideo = false;
                break;
        }
    }

    private void publish() {
        //封面和视频原始文件的上传
        showLoading();
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)){
            if (isVideo) {
                //生成视频封面文件
                FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onChanged(String coverPath) {
                        coverFilePath = coverPath;

                        OneTimeWorkRequest request = getOneTimeWorkRequest(coverPath);
                        coverUploadUUID = request.getId();
                        workRequests.add(request);

                        enqueue(workRequests);
                    }
                });
            }

            //上传视频原始文件
            OneTimeWorkRequest request = getOneTimeWorkRequest(filePath);
            fileUploadUUID = request.getId();
            workRequests.add(request);
            //如果是视频文件则需要等待封面文件生成完毕后再一同提交到任务队列
            //否则 可以直接提交了
            if (!isVideo) {
                enqueue(workRequests);
            }
        }else {
            publishFeed();
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(PublishActivity.this).beginWith(workRequests);
        workContinuation.enqueue();

        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                //block runing enuqued failed susscess finish状态变更回调
                int completedCount = 0;
                int failedCount = 0;
                for (WorkInfo workInfo : workInfos) {
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    UUID uuid = workInfo.getId();
                    if (state == WorkInfo.State.FAILED) {
                        // if (uuid==coverUploadUUID)是错的
                        if (uuid.equals(coverUploadUUID)) {
                            showToast(getString(R.string.file_upload_cover_message));
                        } else if (uuid.equals(fileUploadUUID)) {
                            showToast(getString(R.string.file_upload_original_message));
                        }
                        failedCount++;
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (uuid.equals(coverUploadUUID)) {
                            coverUploadUrl = fileUrl;
                        } else if (uuid.equals(fileUploadUUID)) {
                            fileUploadUrl = fileUrl;
                        }
                        completedCount++;
                    }
                }

                if (completedCount >= workInfos.size()) {
                    publishFeed();
                } else if (failedCount > 0) {
                    dismissLoading();
                }
            }
        });
    }


    private OneTimeWorkRequest getOneTimeWorkRequest(String coverPath) {
        Data inputData = new Data.Builder()
                .putString("file", filePath)
                .build();

        OneTimeWorkRequest build = new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
                .build();
        return build;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RESULT_OK&&requestCode==CaptureActivity.REQ_CAPTURE&&data!=null){
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        mBinding.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.startActivityForResult(PublishActivity.this, filePath, isVideo, null);
            }
        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", coverUploadUrl)
                .addParam("fileUrl", fileUploadUrl)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", MyUserManager.get().getUserId())
                .addParam("tagId", mTagList == null ? 0 : mTagList.tagId)
                .addParam("tagTitle", mTagList == null ? "" : mTagList.title)
                .addParam("feedText", mBinding.inputView.getText().toString())
                .addParam("feedType", isVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        showToast(getString(R.string.feed_publisj_success));
                        PublishActivity.this.finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                        dismissLoading();
                    }
                });
    }


    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.publish_exit_message))
                .setNegativeButton(getString(R.string.publish_exit_action_cancel),null)
                .setPositiveButton(getString(R.string.publish_exit_action_ok),(dialog,which)->{
                    dialog.dismiss();
                    PublishActivity.this.finish();
                }).create().show();
    }

    private void showToast(String string) {
        if (Looper.myLooper()==Looper.getMainLooper()) {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        }else{
            runOnUiThread(()->{
                Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            });
        }
    }

    private LoadingDialog mLoadingDialog = null;
    private void showLoading(){
        if (mLoadingDialog == null){
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setLoadingText(getString(R.string.feed_publish_ing));
        }
        mLoadingDialog.show();
    }

}
