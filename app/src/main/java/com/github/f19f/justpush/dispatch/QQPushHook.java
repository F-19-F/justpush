package com.github.f19f.justpush.dispatch;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QQPushHook {
    public static void printStack() {
        StringWriter errors = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(errors));
        XposedBridge.log(errors.toString());
    }

    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        findAndHookMethod("com.tencent.mobileqq.qfix.QFixApplication", lpparam.classLoader, "onCreate", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//              进程名为推送服务时不拉起QQ主进程
                if (lpparam.processName.endsWith("pushservice")) {
                    return null;
                }
                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        });
//        findAndHookMethod("android.content.ContextWrapper", lpparam.classLoader, "sendBroadcast", Intent.class, new XC_MethodReplacement() {
//            @Override
//            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//
//                Intent i = (Intent) param.args[0];
//                String action = i.getAction();
//                if (action != null) {
//                    switch (action) {
////                      到后台时停止所有服务
//                        case "mqq.intent.action.QQ_BACKGROUND":
//                            ContextWrapper contextWrapper = (ContextWrapper) param.thisObject;
//                            ActivityManager manager = (ActivityManager) contextWrapper.getSystemService(Context.ACTIVITY_SERVICE);
//                            int id = android.os.Process.myPid();
//                            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//                                if(service.pid!=id){
//                                    android.os.Process.killProcess(service.pid);
//                                }
//                            }
//                            System.exit(0);
//                            break;
//                    }
//                }
//                printStack();
//                return XposedBridge.invokeOriginalMethod(param.method,param.thisObject,param.args);
//            }
//        });
    }
}