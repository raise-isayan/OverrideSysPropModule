package com.example.overridesysprop;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainModule implements IXposedHookLoadPackage {

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        XposedBridge.log(lparam.packageName);
        System.setProperty("system.certs.enabled","true");
    }

}
