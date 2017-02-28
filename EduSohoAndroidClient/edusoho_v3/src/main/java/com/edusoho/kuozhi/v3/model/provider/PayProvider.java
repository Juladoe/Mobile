package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

/**
 * Created by suju on 16/9/1.
 */
public class PayProvider extends ModelProvider {

    public PayProvider(Context context) {
        super(context);
    }

    public ProviderListener getPayOrder() {
        ProviderListener providerListener = new ProviderListener(){};
        return providerListener;
    }

    public ProviderListener checkPayResult() {
        ProviderListener providerListener = new ProviderListener(){};
        return providerListener;
    }
}
