package com.edusoho.longinus.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;

/**
 * Created by suju on 17/1/23.
 */

public class CPUUtil {

    private static final String TAG = "CPUUtil";

    public static boolean hasX86CPU() {
        CpuType cpuType = getCpuType();
        return cpuType.hasX86;
    }

    private static CpuType getCpuType() {
        CpuType cpuType = new CpuType();
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abis = getABIList21();
        else
            abis = getABIList();

        for (String abi : abis) {
            if (abi.equals("x86")) {
                Log.e(TAG, "is X86");
                cpuType.hasX86 = true;
            } else if (abi.equals("x86_64")) {
                cpuType.hasX86 = true;
                cpuType.is64bits = true;
            } else if (abi.equals("armeabi-v7a")) {
                Log.e(TAG, "is ARMV7");
                cpuType.hasArmV7 = true;
                cpuType.hasArmV6 = true; /* Armv7 is backwards compatible to < v6 */
            } else if (abi.equals("armeabi")) {
                cpuType.hasArmV6 = true;
            } else if (abi.equals("arm64-v8a")) {
                cpuType.hasNeon = true;
                cpuType.hasArmV6 = true;
                cpuType.hasArmV7 = true;
                cpuType.is64bits = true;
            }
        }
        return cpuType;
    }

    public static boolean hasX86Library(Context context) {
        File customLibraryDir = context.getDir("lib", Context.MODE_PRIVATE);
        File lib = new File(customLibraryDir, "libpldroidplayer.so");
        if (lib.exists() && lib.canRead()) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String[] getABIList21() {
        final String[] abis = Build.SUPPORTED_ABIS;
        if (abis == null || abis.length == 0)
            return getABIList();
        return abis;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static String[] getABIList() {
        final boolean hasABI2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
        final String[] abis = new String[hasABI2 ? 2 : 1];
        abis[0] = android.os.Build.CPU_ABI;
        if (hasABI2)
            abis[1] = android.os.Build.CPU_ABI2;
        return abis;
    }

    static class CpuType {
        public boolean hasNeon = false;
        public boolean hasFpu = false;
        public boolean hasArmV6 = false;
        public boolean hasArmV7 = false;
        public boolean hasMips = false;
        public boolean hasX86 = false;
        public boolean is64bits = false;
    }
}
