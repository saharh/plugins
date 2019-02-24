package com.applaudsoft.wabi.virtual_number.fragments.dialog;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import io.flutter.plugins.firebasemessaging.R;

public class WACodeFeedbackActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_code_feedback);
//        ButterKnife.bind(this);
        setupViews(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setupViews(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            BaseDialogFragment.dismissAnyShownDialog(supportFragmentManager);
            WACodeFeedbackDialogFragment newFragment = WACodeFeedbackDialogFragment.newInstance();
            newFragment.showAllowingStateloss(supportFragmentManager);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
