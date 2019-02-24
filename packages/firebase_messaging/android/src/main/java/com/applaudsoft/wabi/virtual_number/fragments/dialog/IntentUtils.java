package com.applaudsoft.wabi.virtual_number.fragments.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Created by saharh on 03/08/2015.
 */
public class IntentUtils {
//    public static void openComposeEmailActivity(Context context, String[] addresses, String subject) {
//        openComposeEmailActivity(context, addresses, subject, "");
//    }
//
//    public static void openComposeEmailActivity(Context context, String[] addresses, String subject, String body) {
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
//        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        intent.putExtra(Intent.EXTRA_TEXT, body);
//        startIntentIfPossible(context, intent);
//    }
//
//    public static void openWhatsApp(Context context, String message) {
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.setPackage("com.whatsapp");
//        intent.putExtra(Intent.EXTRA_TEXT, message);
//        startIntentIfPossible(context, intent);
//    }
//
//    public static void openWhatsAppBusiness(Context context, String contactPhoneNumber, String message) {
//        contactPhoneNumber = contactPhoneNumber.replace("+", "").replace(" ", "").replace("-", "");
//        Intent intent = new Intent("android.intent.action.MAIN");
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setPackage("com.whatsapp.w4b");
//        try {
//            if (StringUtils.isNotBlank(message)) {
//                message = URLEncoder.encode(message, "UTF-8");
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String url = "https://api.whatsapp.com/send?phone=" + contactPhoneNumber;
//        if (StringUtils.isNotBlank(message)) {
//            url += "&text=" + message;
//        }
//        intent.setData(Uri.parse(url));
//        startIntentIfPossible(context, intent);
//    }
//
//
//    private static void startIntentIfPossible(Context context, Intent intent) {
//        if (intent != null && isActivityAvailableForIntent(context, intent)) {
//            context.startActivity(intent);
//        }
//    }
//
//    private static boolean isActivityAvailableForIntent(Context context, Intent intent) {
//        return intent.resolveActivity(context.getPackageManager()) != null;
//    }

    public static void openThisAppPlayStorePage(Context context) {
//        context = context != null ? context : WabiApplication.getAppContext();
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        openAppPlayStorePage(context, appPackageName);
    }

    public static void openAppPlayStorePage(Context context, String appPackageName) {
        openAppPlayStorePage(context, appPackageName, null);
    }

    public static void openAppPlayStorePage(Context context, String appPackageName, @Nullable String appendToUrl) {
        try {
            String uri = "market://details?id=" + appPackageName;
            if (StringUtils.isNotBlank(appendToUrl)) {
                uri += appendToUrl;
            }
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        } catch (ActivityNotFoundException anfe) {
            try {
                String uri = "https://play.google.com/store/apps/details?id=" + appPackageName;
                if (StringUtils.isNotBlank(appendToUrl)) {
                    uri += appendToUrl;
                }
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            } catch (ActivityNotFoundException anfe2) {
//                Timber.e(anfe2, "Couldn\'t open Play store page");
            }
        }
    }

}
