package com.github.f19f.justpush;
import com.github.f19f.justpush.dispatch.MilletHook;
import com.github.f19f.justpush.dispatch.QQPushHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        switch (packageName){
            case "com.tencent.mobileqq":
                new QQPushHook().handleLoadPackage(lpparam);
                break;
            case "com.miui.powerkeeper":
                MilletHook.handleLoadPackage(lpparam);
            default:
                break;
        }
    }
}
