package com.github.f19f.justpush.dispatch;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QQPushHook {

    public String procName;
    public static ArrayList<Object> sockets;
    public static Context context;
    public AtomicBoolean toClose;
    public final static String BRD_CLOSE_SOCKET = "BRD_CLOSE_SOCKET";
    public final static String BRD_RELEASE_SOCKET = "BRD_RELEASE_SOCKET";

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        this.procName = lpparam.processName;
        hookServices(lpparam);
        sockets = new ArrayList<>();
        toClose = new AtomicBoolean(false);
        XposedBridge.hookAllConstructors(lpparam.classLoader.loadClass("java.net.Socket"), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (toClose.get()) {
                    XposedHelpers.callMethod(param.thisObject, "close");
                    return;
                }
                if (isMsf()) {
                    sockets.add(param.thisObject);
                }
            }
        });
        XposedBridge.hookAllConstructors(lpparam.classLoader.loadClass("java.net.DatagramSocket"), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (toClose.get()) {
                    XposedHelpers.callMethod(param.thisObject, "close");
                    return;
                }
                if (isMsf()) {
                    sockets.add(param.thisObject);
                }
            }
        });
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", lpparam.classLoader, "doOnResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                context.sendBroadcast(new Intent(BRD_RELEASE_SOCKET));
            }
        });
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.msf.service.MsfService", lpparam.classLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("mqq.intent.action.QQ_BACKGROUND");
                intentFilter.addAction("mqq.intent.action.QQ_FOREGROUND");
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (intent.getAction()) {
                            case "mqq.intent.action.QQ_BACKGROUND":
                                context.sendBroadcast(new Intent(BRD_CLOSE_SOCKET));
                                break;
                            case "mqq.intent.action.QQ_FOREGROUND":
                                context.sendBroadcast(new Intent(BRD_RELEASE_SOCKET));
                                break;
                        }

                    }
                }, intentFilter);
            }
        });
    }

    public void hookServices(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("com.tencent.mobileqq.qfix.QFixApplication", lpparam.classLoader, "onCreate", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//              进程名为推送服务时不拉起QQ主进程
                context = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                if (lpparam.processName.endsWith("pushservice")) {
                    return null;
                }
                if (isMsf()) {
                    HandlerThread handlerThread = new HandlerThread("CloseSocket");
                    handlerThread.start();
                    Handler handler = new Handler(handlerThread.getLooper());
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(BRD_CLOSE_SOCKET);
                    intentFilter.addAction(BRD_RELEASE_SOCKET);
                    context.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            switch (intent.getAction()) {
                                case BRD_CLOSE_SOCKET:
                                    toClose.set(true);
                                    for (Object socket : sockets) {
//                                    Log.d("QQ_BRD","close socket");
                                        handler.post(() -> {
                                            XposedHelpers.callMethod(socket, "close");
                                        });
                                    }
                                    sockets.clear();

                                    break;
                                case BRD_RELEASE_SOCKET:
                                    toClose.set(false);
                                    break;
                            }

                        }
                    }, intentFilter);
                }
                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        });
    }

    public boolean isMsf() {
        return procName.contains("MSF");
    }
}