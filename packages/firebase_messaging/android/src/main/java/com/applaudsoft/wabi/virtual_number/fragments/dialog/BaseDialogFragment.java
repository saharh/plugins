package com.applaudsoft.wabi.virtual_number.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_NONE;


/**
 * Created by Sahar on 16/05/2016.
 */
public abstract class BaseDialogFragment extends DialogFragment {

    public static final String DIALOG_FRAG_TAG = "dialog";

    public void show(FragmentTransaction ft) {
        show(ft, BaseDialogFragment.DIALOG_FRAG_TAG);
    }

    public void showAllowingStateloss(FragmentManager manager) {
        showAllowingStateloss(manager, DIALOG_FRAG_TAG);
    }

    public void showAllowingStateloss(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.setTransition(TRANSIT_NONE);
        ft.setCustomAnimations(0, 0);
        ft.commitAllowingStateLoss();
    }

    public static BaseDialogFragment currentShownDialog(FragmentManager supportFragmentManager) {
        return (BaseDialogFragment) supportFragmentManager.findFragmentByTag(DIALOG_FRAG_TAG);
    }

    public static void dismissAnyShownDialog(FragmentManager supportFragmentManager) {
        BaseDialogFragment prev = currentShownDialog(supportFragmentManager);
        if (prev != null) {
            prev.dismissAllowingStateLoss();
        }
    }

    public static boolean anyDialogShown(FragmentManager supportFragmentManager) {
        return currentShownDialog(supportFragmentManager) != null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null && !shouldShowTitle()) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Icepick.saveInstanceState(this, outState);
    }

    protected boolean shouldShowTitle() {
        return false;
    }

}
