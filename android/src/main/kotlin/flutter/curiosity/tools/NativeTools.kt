package flutter.curiosity.toolsimport android.Manifestimport android.annotation.SuppressLintimport android.app.Activityimport android.app.PendingIntentimport android.content.Contextimport android.content.Intentimport android.content.pm.ApplicationInfoimport android.content.pm.PackageInfoimport android.content.res.Resourcesimport android.location.LocationManagerimport android.net.Uriimport android.os.Buildimport android.os.Environmentimport android.os.Processimport android.provider.Settingsimport androidx.collection.ArrayMapimport androidx.core.content.FileProviderimport flutter.curiosity.CuriosityPlugin.Companion.callimport flutter.curiosity.CuriosityPlugin.Companion.installApkCodeimport flutter.curiosity.CuriosityPlugin.Companion.installPermissionimport java.io.Fileimport kotlin.collections.ArrayListimport kotlin.collections.setimport kotlin.system.exitProcessobject NativeTools {    private fun getBarHeight(barName: String?, context: Context): Float {        val resources: Resources = context.resources        val resourceId = resources.getIdentifier(barName, "dimen", "android")        return resources.getDimensionPixelSize(resourceId).toFloat()    }    /**     * 获取app路径和信息     */    fun getAppInfo(context: Context): Map<String, Any> {        val pm = context.packageManager        val info = pm.getPackageInfo(context.packageName, 0)        val map: MutableMap<String, Any> = HashMap()        val filesDir = context.filesDir.path        val cacheDir = context.cacheDir.path        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {            map["externalCacheDir"] = context.externalCacheDir?.path.toString()            map["externalFilesDir"] =                context.getExternalFilesDir(null)?.path.toString()            map["externalStorageDirectory"] =                Tools.getExternalDirectory(null, context)            map["DIRECTORY_DCIM"] =                Tools.getExternalDirectory(Environment.DIRECTORY_DCIM, context)            map["DIRECTORY_DOWNLOADS"] = Tools.getExternalDirectory(                Environment.DIRECTORY_DOWNLOADS,                context            )            map["DIRECTORY_MOVIES"] = Tools.getExternalDirectory(                Environment.DIRECTORY_MOVIES,                context            )            map["DIRECTORY_MUSIC"] =                Tools.getExternalDirectory(Environment.DIRECTORY_MUSIC, context)            map["DIRECTORY_PICTURES"] = Tools.getExternalDirectory(                Environment.DIRECTORY_PICTURES,                context            )            map["DIRECTORY_ALARMS"] = Tools.getExternalDirectory(                Environment.DIRECTORY_ALARMS,                context            )            map["DIRECTORY_DOCUMENTS"] = Tools.getExternalDirectory(                Environment.DIRECTORY_DOCUMENTS,                context            )            map["DIRECTORY_NOTIFICATIONS"] = Tools.getExternalDirectory(                Environment.DIRECTORY_NOTIFICATIONS,                context            )            map["DIRECTORY_RINGTONES"] = Tools.getExternalDirectory(                Environment.DIRECTORY_RINGTONES,                context            )            map["DIRECTORY_PODCASTS"] = Tools.getExternalDirectory(                Environment.DIRECTORY_PODCASTS,                context            )        } else {            map["externalFilesDir"] = filesDir            map["externalCacheDir"] = cacheDir        }        map["filesDir"] = filesDir        map["cacheDir"] = cacheDir        map["sdkVersion"] = Build.VERSION.SDK_INT        map["appName"] = info.applicationInfo.loadLabel(pm).toString()        map["packageName"] = info.packageName        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {            map["versionCode"] = info.longVersionCode        } else {            map["versionCode"] = info.versionCode        }        map["versionName"] = info.versionName        map["firstInstallTime"] = info.firstInstallTime        map["lastUpdateTime"] = info.lastUpdateTime        map["statusBarHeight"] = getBarHeight("status_bar_height", context)        map["navigationBarHeight"] =            getBarHeight("navigation_bar_height", context)        return map    }    /**     * 获取设备信息     */    @SuppressLint("HardwareIds")    fun getDeviceInfo(context: Context): Map<String, Any> {        val map: MutableMap<String, Any> = HashMap()        map["board"] = Build.BOARD        map["bootloader"] = Build.BOOTLOADER        map["brand"] = Build.BRAND        map["device"] = Build.DEVICE        map["display"] = Build.DISPLAY        map["fingerprint"] = Build.FINGERPRINT        map["hardware"] = Build.HARDWARE        map["host"] = Build.HOST        map["id"] = Build.ID        map["manufacturer"] = Build.MANUFACTURER        map["model"] = Build.MODEL        map["product"] = Build.PRODUCT        map["tags"] = Build.TAGS        map["type"] = Build.TYPE        map["isEmulator"] = !Tools.isEmulator()        map["androidId"] = Settings.Secure.getString(            context.contentResolver,            Settings.Secure.ANDROID_ID        )        map["isDeviceRoot"] = Tools.isDeviceRoot()        val version: MutableMap<String, Any> = HashMap()        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {            version["baseOS"] = Build.VERSION.BASE_OS            version["previewSdkInt"] = Build.VERSION.PREVIEW_SDK_INT            version["securityPatch"] = Build.VERSION.SECURITY_PATCH        }        version["codename"] = Build.VERSION.CODENAME        version["incremental"] = Build.VERSION.INCREMENTAL        version["release"] = Build.VERSION.RELEASE        version["sdkInt"] = Build.VERSION.SDK_INT        map["version"] = version        return map    }    /**     * 获取应用列表     */    fun getInstalledApp(context: Context): ArrayList<MutableMap<String, Any>> {        val list: ArrayList<MutableMap<String, Any>> = ArrayList()        val pm = context.packageManager        val packages: MutableList<PackageInfo> =            pm.getInstalledPackages(0) // 获取所有已安装程序的包信息        for (packageInfo in packages) {            val info: MutableMap<String, Any> = ArrayMap()            info["isSystemApp"] = (packageInfo.applicationInfo.flags and                    ApplicationInfo.FLAG_SYSTEM) != 0            info["versionName"] = packageInfo.versionName            info["appName"] = packageInfo.applicationInfo.loadLabel(pm)            info["packageName"] = packageInfo.packageName            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {                info["versionCode"] = packageInfo.longVersionCode            } else {                info["versionCode"] = packageInfo.versionCode            }            info["lastUpdateTime"] = packageInfo.lastUpdateTime            list.add(info)        }        return list    }    /**     * 安装apk     */    fun installApp(context: Context, activity: Activity) {        //判断是否开启安装权限        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {            if (!context.packageManager.canRequestPackageInstalls()) {                //权限没有打开则去手动打开                val packageURI = Uri.parse("package:" + context.packageName)                val intent = Intent(                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,                    packageURI                )                activity.startActivityForResult(intent, installPermission)                return            }        }        //安装        val apkPath = call.argument<String>("apkPath") ?: return        val file = File(apkPath)        val intent = Intent(Intent.ACTION_VIEW)        //版本在7.0以上是不能直接通过uri访问的        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件            val apkUri = FileProvider.getUriForFile(                context,                context.packageName + ".provider",                file            )            //添加这一句表示对目标应用临时授权该Uri所代表的文件            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)            intent.setDataAndType(                apkUri,                "application/vnd.android.package-archive"            )        } else {            intent.setDataAndType(                Uri.fromFile(file),                "application/vnd.android.package-archive"            )        }        activity.startActivityForResult(intent, installApkCode)    }    /**     * 获取路径文件和文件夹大小     */    fun getFilePathSize(): String? {        val filePath = call.argument<String>("filePath") ?: return null        return if (Tools.isDirectoryExist(filePath)) {            val file = File(filePath)            if (file.isDirectory) {                Tools.getDirectorySize(file)            } else {                Tools.getFileSize(file)            }        } else {            Tools.resultInfo("NotFile")        }    }    /**     * 退出app     */    fun exitApp() {        //杀死进程，否则就算退出App，App处于空进程并未销毁，再次打开也不会初始化Application        //从而也不会执行getJSBundleFile去更换bundle的加载路径 !!!        Process.killProcess(Process.myPid())        exitProcess(0)    }    /**     * 判断手机是否安装某个应用     *     * @return true：安装，false：未安装 error 参数参数     */    fun isInstallApp(context: Context): Boolean {        val packageName = call.arguments        val packages: MutableList<PackageInfo> =            context.packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息        for (packageInfo in packages) {            if (packageName == packageInfo.packageName) {                return true            }        }        return false    }    /**     * 跳转至应用商店     */    fun openAppMarket(activity: Activity): Boolean {        val packageName = call.argument<String>("packageName")        if (packageName == null || packageName.isEmpty()) return false        val marketPackageName = call.argument<String>("marketPackageName")        val uri = Uri.parse("market://details?id=$packageName")        val intent = Intent(Intent.ACTION_VIEW, uri)        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)        if (marketPackageName != null && marketPackageName != "") {            // 如果没给市场的包名，则系统会弹出市场的列表让你进行选择。            intent.setPackage(marketPackageName)        }        activity.startActivity(intent)        return true;    }    /**     * 拨打电话     */    fun callPhone(context: Context, activity: Activity): Boolean {        if (Tools.checkPermission(Manifest.permission.CALL_PHONE, context)) {            val phoneNumber = call.argument<String>("phoneNumber")            val directDial = call.argument<Boolean>("directDial")            val intent = Intent()            if (directDial!!) {                intent.action = Intent.ACTION_CALL            } else {                intent.action = Intent.ACTION_DIAL            }            intent.data = Uri.parse("tel:$phoneNumber")            activity.startActivity(intent)            return true        }        return false    }    /**     * 调用系统分享     */    fun systemShare(activity: Activity): String {        val type = call.argument<String>("type")        val title = call.argument<String>("title") ?: ""        val content = call.argument<String>("content")        val imagesPath = call.argument<ArrayList<String>>("imagesPath")        if (type != null) {            var shareIntent = Intent()            when (type) {                "text", "url" -> {                    if (content == null) return Tools.resultInfo("not find text")                    shareIntent.action = Intent.ACTION_SEND                    shareIntent.type = "text/plain"                    shareIntent.putExtra(Intent.EXTRA_TEXT, content)                }                "image" -> {                    if (content == null) return Tools.resultInfo("not find image")                    //将mipmap中图片转换成Uri                    val imgUri = Uri.parse(content)                    shareIntent.action = Intent.ACTION_SEND                    shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri)                    shareIntent.type = "image/*"                }                "images" -> {                    if (imagesPath == null) return Tools.resultInfo("not find imagesPath")                    if (imagesPath.size == 0) return Tools.resultInfo("imagesPath size is 0")                    val imgUris = ArrayList<Uri>()                    for (value in imagesPath) {                        imgUris.add(Uri.parse(value))                    }                    shareIntent.action = Intent.ACTION_SEND_MULTIPLE                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)                    shareIntent.putParcelableArrayListExtra(                        Intent.EXTRA_STREAM,                        imgUris                    )                    shareIntent.type = "image/*"                }            }            if (content != null || imagesPath != null) {                shareIntent = Intent.createChooser(shareIntent, title)                activity.startActivity(shareIntent)                return Tools.resultSuccess()            }            return Tools.resultInfo("not find text")        } else {            return Tools.resultInfo("not find type")        }    }    /**     * 跳转到设置页面让用户自己手动开启     */    fun jumpGPSSetting(context: Context, activity: Activity): Boolean {        val isOpen: Boolean = getGPSStatus(context) //判断GPS是否打开        if (!isOpen) {            try {                val locationIntent =                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)                activity.startActivity(locationIntent)                return true            } catch (ex: Exception) {            }        }        return false    }    /**     * 跳转到系统自带的设置     */    fun jumpSystemSetting(activity: Activity): Boolean {        try {            val settingType = call.argument<String>("settingType")            var intent = Intent()            if (settingType == "wifi") {                intent = Intent(Settings.ACTION_WIFI_SETTINGS)            } else if (settingType == "wifiIp") {                intent = Intent(Settings.ACTION_WIFI_IP_SETTINGS)            } else if (settingType == "location") {                intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)            } else if (settingType == "security") {                intent = Intent(Settings.ACTION_SECURITY_SETTINGS)            } else if (settingType == "passwordSecurity") {                intent = Intent(Settings.ACTION_SECURITY_SETTINGS)            } else if (settingType == "cellularNetwork") {                intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)            } else if (settingType == "time") {                intent = Intent(Settings.ACTION_DATE_SETTINGS)            } else if (settingType == "displayBrightness") {                intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)            } else if (settingType == "notification") {                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {                    intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)                        .putExtra(                            Settings.EXTRA_APP_PACKAGE,                            activity.packageName                        )                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build                        .VERSION.SDK_INT < Build.VERSION_CODES.O                ) {                    intent = Intent(                        Settings                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS                    )                }            } else if (settingType == "soundVibration") {                intent = Intent(Settings.ACTION_SOUND_SETTINGS)            } else if (settingType == "internalStorage") {                intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)            } else if (settingType == "battery") {                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                    intent = Intent(                        Settings                            .ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS                    )                }            } else if (settingType == "nfc") {                intent = Intent(Settings.ACTION_NFC_SETTINGS)            } else if (settingType == "localeLanguage") {                intent = Intent(Settings.ACTION_LOCALE_SETTINGS)            } else if (settingType == "deviceInfo") {                intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)            } else if (settingType == "applicationDevelopment") {                intent =                    Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)            } else if (settingType == "networkOperator") {                intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)            } else if (settingType == "addAccount") {                intent = Intent(Settings.ACTION_ADD_ACCOUNT)            } else if (settingType == "dataRoaming") {                intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)            } else if (settingType == "airplaneMode") {                intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)            } else {                intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)                intent.data =                    Uri.fromParts("package", activity.packageName, null)            }            activity.startActivity(intent)            return true        } catch (ex: Exception) {        }        return false    }    /**     * 跳转到应用权限设置     */    fun jumpAppSetting(context: Context, activity: Activity): Boolean {        try {            val settingsIntent = Intent()            settingsIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS            settingsIntent.addCategory(Intent.CATEGORY_DEFAULT)            settingsIntent.data = Uri.parse("package:" + context.packageName)            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)            activity.startActivity(settingsIntent)            return true        } catch (ex: Exception) {        }        return false    }    /**     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的     * @return true 表示开启     */    fun getGPSStatus(context: Context): Boolean {        val locationManager: LocationManager =            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）        val gps: Boolean =            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）        val network: Boolean =            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)        return gps || network    }    /**     * 强制帮用户打开GPS     * 无效     */    fun openGPS(context: Context) {        val intent = Intent()        intent.setClassName(            "com.android.settings",            "com.android.settings.widget.SettingsAppWidgetProvider"        )        intent.addCategory("android.intent.category.ALTERNATIVE")        intent.data = Uri.parse("custom:3")        try {            PendingIntent.getBroadcast(context, 0, intent, 0).send()        } catch (e: PendingIntent.CanceledException) {            e.printStackTrace()        }    }}