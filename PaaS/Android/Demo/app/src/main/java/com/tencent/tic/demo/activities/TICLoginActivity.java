package com.tencent.tic.demo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tencent.imsdk.TIMManager;
import com.tencent.teduboard.TEduBoardController;
import com.tencent.tic.core.TICManager;
import com.tencent.tic.demo.R;
import com.tencent.tic.demo.TRTCGetUserIDAndUserSig;
import com.tencent.trtc.TRTCCloud;

import java.util.ArrayList;

public class TICLoginActivity extends BaseActvity implements CompoundButton.OnCheckedChangeListener {
    private TRTCGetUserIDAndUserSig mUserInfoLoader;

    Spinner spinnerAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_layout);
        tvLog = (TextView) findViewById(R.id.tv_login_log);
        spinnerAccount = (Spinner) findViewById(R.id.spinner_account);

        // 如果配置有config文件，则从config文件中选择userId
        mUserInfoLoader = new TRTCGetUserIDAndUserSig(this);
        final ArrayList<String> userIds = mUserInfoLoader.getUserIdFromConfig();
        final ArrayList<String> userSigs = mUserInfoLoader.getUserSigFromConfig();

        if (userIds != null && userIds.size() > 0) {
            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userIds);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            String useId = resumeFromCache();
            int pos = userIds.indexOf(useId);
            if (pos < 0 || pos >= userIds.size()) {
                pos = 0;
            }
            mUserID = userIds.get(pos);
            mUserSig = userSigs.get(pos);

            spinnerAccount.setAdapter(spinnerAdapter);
            spinnerAccount.setSelection(pos, false);
            spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mUserID = userIds.get(position);
                    mUserSig = userSigs.get(position);

                    writeToCache(mUserID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        //版本信息
        TextView trtcVersionTV = (TextView)findViewById(R.id.tv_version_trtc);
        trtcVersionTV.setText("TRTC: " + TRTCCloud.getSDKVersion());
        TextView imVersionTV = (TextView)findViewById(R.id.tv_version_im);
        imVersionTV.setText("IMSDK: " + TIMManager.getInstance().getVersion());
        TextView boardVersionTV = (TextView)findViewById(R.id.tv_version_board);
        boardVersionTV.setText("TEduBoard: " + TEduBoardController.getVersion());

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            onLogoutClick(null);
        }
    }

    /**
     * 登录
     */
    public void onLoginClick(View v) {
        // 默认走的是云上环境
        if (TextUtils.isEmpty(mUserID) || TextUtils.isEmpty(mUserSig)) {
            postToast("请检查账号信息是否正确");
            return;
        }

        mTicManager.login(mUserID, mUserSig, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast(mUserID + ":登录成功" , true);
                launchClassManagerActivity();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast(mUserID+ ":登录失败, err:" + errCode + "  msg: " + errMsg);
            }
        });
    }

    public void writeToCache(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, userId);
        editor.commit();
    }

    public String resumeFromCache() {
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        String userId = sharedPreferences.getString(USER_ID, "");
        return userId;
    }

    public void onLogoutClick(View v) {

        mTicManager.logout(new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast(mUserID + ":登出成功" , true);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("登出失败, err:" + errCode + " msg: " + errMsg);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    private void launchClassManagerActivity() {
        Intent intent = new Intent(this, TICClassManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(TICClassManagerActivity.USER_ID, mUserID);
        intent.putExtra(TICClassManagerActivity.USER_SIG, mUserSig);
        startActivity(intent);
    }

}
