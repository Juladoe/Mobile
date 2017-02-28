package com.edusoho.kuozhi.v3.util;

import android.text.TextUtils;

/**
 * Created by howzhi on 15/9/24.
 */
public class ServiceProviderUtil {

    private enum SPFragmentType {
       NEWS("Article"), EMPTY("");

       private String name;
       SPFragmentType(String name) {
           this.name = name;
       }

       public static SPFragmentType parse(String type) {
           try {
               return SPFragmentType.valueOf(type.toUpperCase());
           } catch (Exception e) {
               e.printStackTrace();
           }

           return EMPTY;
       }

        @Override
        public String toString() {
            if (TextUtils.isEmpty(name)) {
                return "";
            }
            return name + "Fragment";
        }
    }

    public static String coverFragmentName(String type) {
        SPFragmentType fragmentType = SPFragmentType.parse(type);
        return fragmentType.toString();
    }
}
