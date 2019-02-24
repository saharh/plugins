package com.applaudsoft.wabi.virtual_number.fragments.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import io.flutter.plugins.firebasemessaging.R;

/**
 * Created by Sahar on 08/12/2016.
 */

public class WACodeFeedbackDialogFragment extends BaseDialogFragment {

    static WACodeFeedbackDialogFragment newInstance() {
        return new WACodeFeedbackDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //        ButterKnife.bind(this, rootView);
        return inflater.inflate(R.layout.fragment_wa_code_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.it_worked_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItWorkedClicked();
            }
        });
        view.findViewById(R.id.got_code_didnt_work_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGotCodeDidntWorkClicked();
            }
        });
        view.findViewById(R.id.didnt_get_code_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDidntGetCodeClicked();
            }
        });
        view.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSkipClicked();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogAppTheme);
        Dialog dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
            }
        };
        Window window = dialog.getWindow();
        if (window != null && !shouldShowTitle()) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

//    @OnClick(R.id.it_worked_btn)
    void onItWorkedClicked() {
        FragmentManager supportFragmentManager = getFragmentManager();
        BaseDialogFragment.dismissAnyShownDialog(supportFragmentManager);
        WACodeWorkedDialogFragment newFragment = WACodeWorkedDialogFragment.newInstance();
        newFragment.showAllowingStateloss(supportFragmentManager);
    }

//    @OnClick(R.id.got_code_didnt_work_btn)
    void onGotCodeDidntWorkClicked() {
        FragmentManager supportFragmentManager = getFragmentManager();
        BaseDialogFragment.dismissAnyShownDialog(supportFragmentManager);
        WACodeDidntWorkDialogFragment newFragment = WACodeDidntWorkDialogFragment.newInstance();
        newFragment.showAllowingStateloss(supportFragmentManager);
    }

//    @OnClick(R.id.didnt_get_code_btn)
    void onDidntGetCodeClicked() {
        FragmentManager supportFragmentManager = getFragmentManager();
        BaseDialogFragment.dismissAnyShownDialog(supportFragmentManager);
        WACodeDidntWorkDialogFragment newFragment = WACodeDidntWorkDialogFragment.newInstance();
        newFragment.showAllowingStateloss(supportFragmentManager);
    }

//    @OnClick(R.id.skip)
    void onSkipClicked() {
        dismiss();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

}