package com.github.f19f.justpush;
import com.github.f19f.justpush.dispatch.QQPushHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        switch (packageName){
            case "com.tencent.mobileqq":
                QQPushHook.handleLoadPackage(lpparam);
                break;
            default:
                break;
        }
    }
}
