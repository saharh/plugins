package io.flutter.plugins.inapppurchase;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.flutter.plugin.common.MethodChannel;

import static io.flutter.plugins.inapppurchase.Translator.fromPurchasesList;

class PluginPurchaseListener implements PurchasesUpdatedListener {
    private final MethodChannel channel;

    PluginPurchaseListener(MethodChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        final Map<String, Object> callbackArgs = new HashMap<>();
        callbackArgs.put("responseCode", billingResult.getResponseCode());
        callbackArgs.put("purchasesList", fromPurchasesList(purchases));
        channel.invokeMethod(InAppPurchasePlugin.MethodNames.ON_PURCHASES_UPDATED, callbackArgs);
    }

}
