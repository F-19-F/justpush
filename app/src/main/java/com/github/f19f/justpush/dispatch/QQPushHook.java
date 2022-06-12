package com.github.f19f.justpush.dispatch;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QQPushHook {
    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable{
        findAndHookMethod("com.tencent.mobileqq.qfix.QFixApplication",lpparam.classLoader, "onCreate", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//              进程名为推送服务时不拉起QQ主进程
                if(lpparam.processName.endsWith("pushservice")){
                    return null;
                }
                return XposedBridge.invokeOriginalMethod(param.method,param.thisObject,param.args);
            }
        });
    }
}
