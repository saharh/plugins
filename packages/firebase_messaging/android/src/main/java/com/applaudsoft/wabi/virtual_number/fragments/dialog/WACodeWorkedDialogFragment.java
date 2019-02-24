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
import io.flutter.plugins.firebasemessaging.R;

/**
 * Created by Sahar on 08/12/2016.
 */

public class WACodeWorkedDialogFragment extends BaseDialogFragment {

    public static WACodeWorkedDialogFragment newInstance() {
        return new WACodeWorkedDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //        ButterKnife.bind(this, rootView);
        return inflater.inflate(R.layout.fragment_wa_code_worked, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.rate_app_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRateAppClicked();
            }
        });
        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClicked();
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
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

//    @OnClick(R.id.rate_app_btn)
    public void onRateAppClicked() {
        IntentUtils.openThisAppPlayStorePage(getContext());
    }

//    @OnClick(R.id.share_app_btn)
//    public void onShareAppClicked() {
//        FragmentManager supportFragmentManager = getFragmentManager();
//        ShareDialogFragment newFragment = ShareDialogFragment.newInstance();
//        newFragment.showAllowingStateloss(supportFragmentManager);
//    }

//    @OnClick(R.id.done)
    public void onDoneClicked() {
        dismiss();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

}