package com.example.vcam.hook

import android.util.Log
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.xposed.IXposedHookLoadPackage
import com.example.vcam.xposed.IXposedHookZygoteInit
import com.example.vcam.xposed.XC_LoadPackage
import com.example.vcam.xposed.XposedBridge

/**
 * Xposed / LSPosed main entry point declared in assets/xposed_init.
 * Initializes Camera1Hook and Camera2Hook for target hooked applications.
 */
class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        private const val TAG = "GThang-XposedInit"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        Log.i(TAG, "G Thang VCAM Zygote initialized: ${startupParam.modulePath}")
        XposedBridge.log("G Thang VCAM Module Loaded in Zygote")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        if (packageName == "com.example") {
            // Self package
            return
        }

        val config = VcamConfigManager.loadConfigForHook(packageName)
        if (!config.enabled) {
            return
        }

        // Check target app whitelist if specified
        if (config.targetApps.isNotEmpty() && !config.targetApps.contains(packageName)) {
            Log.d(TAG, "Package $packageName is not in target list, skipping")
            return
        }

        Log.i(TAG, "Injecting G Thang Virtual Camera into package: $packageName (process: ${lpparam.processName})")
        XposedBridge.log("G Thang VCAM Active in $packageName")

        // Initialize Camera1 and Camera2 hooks
        Camera1Hook.initHook(lpparam.classLoader)
        Camera2Hook.initHook(lpparam.classLoader)
    }
}
