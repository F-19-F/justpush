package com.github.f19f.justpush.dispatch;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
public class MilletHook {
    public static void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
//        借用一下代码
//        XposedBridge.log("Don't freeze HuaweiPush!");
        Class clazz = lpparam.classLoader.loadClass("com.miui.powerkeeper.millet.MilletUidObserver");
        Method getPkgNameByUid = clazz.getDeclaredMethod("getPkgNameByUid", int.class);
        findAndHookMethod("com.miui.powerkeeper.millet.MilletPolicy", lpparam.classLoader, "isAllowFreeze", int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                String PkgName = (String) getPkgNameByUid.invoke(null,param.args[0]);
//                不要冻结华为推送
                if(PkgName.equals("com.huawei.hwid")){
                    XposedBridge.log("Don't freeze HuaweiPush!");
                    return false;
                }
                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        });
    }
}
