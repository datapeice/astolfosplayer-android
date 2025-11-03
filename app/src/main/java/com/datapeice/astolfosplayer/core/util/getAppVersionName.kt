package com.datapeice.astolfosplayer.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Context.getAppVersionName(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
    } else {
        packageManager.getPackageInfo(packageName, 0).versionName
    } ?: ""
}