package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import java.util.WeakHashMap;

/**
 * Created by howzhi on 15/9/9.
 */
public class ProviderFactory {

    private ProviderFactory() {
        mProviderHashMap = new WeakHashMap<>();
    }

    private WeakHashMap<String, ModelProvider> mProviderHashMap;
    private static ProviderFactory mFactory;

    public static synchronized ProviderFactory getFactory() {
        if (mFactory == null) {
            mFactory = new ProviderFactory();
        }

        return mFactory;
    }

    private void inject(String name, ModelProvider provider) {
        mProviderHashMap.put(name, provider);
    }

    public ModelProvider getProvider(String name) {
        return mProviderHashMap.get(name);
    }

    public ModelProvider create(Class targetClass, Context context) {
        ModelProvider provider = getProvider(targetClass.getSimpleName());
        if (provider == null) {
            try {
                provider = (ModelProvider) targetClass.getConstructor(Context.class).newInstance(context);
            } catch (Exception e) {
                //nothing
            }

            inject(targetClass.getSimpleName(), provider);
        }

        return provider;
    }
}
