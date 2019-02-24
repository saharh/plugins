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

public class WACodeDidntWorkDialogFragment extends BaseDialogFragment {

    public static WACodeDidntWorkDialogFragment newInstance() {
        return new WACodeDidntWorkDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wa_code_didnt_work, container, false);
//        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (window != null && !shouldShowTitle()) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

//    @OnClick(R.id.send_feedback_btn)
//    public void onSendFeedbackClicked() {
//        analytics().logEvent("wa_code_fail_action_send_feedback");
//        IntentUtils.openSendFeedbackMail(getContext(), wabiService.lastUserData(), "Code Didn\'t Work");
//    }

//    @OnClick(R.id.done)
    public void onDoneClicked() {
        dismiss();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }


}