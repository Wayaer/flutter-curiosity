package flutter.curiosity.toolsimport android.Manifestimport android.content.Intentimport android.content.pm.PackageInfoimport android.content.res.Resourcesimport android.net.Uriimport android.os.Buildimport android.os.Processimport androidx.core.content.FileProviderimport flutter.curiosity.CuriosityPlugin.Companion.activityimport flutter.curiosity.CuriosityPlugin.Companion.callimport flutter.curiosity.CuriosityPlugin.Companion.contextimport java.io.Fileimport kotlin.system.exitProcessobject NativeTools {    fun getBarHeight(barName: String?): Float {        val resources: Resources = context.resources        val resourceId = resources.getIdentifier(barName, "dimen", "android")        return resources.getDimensionPixelSize(resourceId).toFloat()    }    /**     * 安装apk     */    fun installApp(): String {        val apkPath = call.argument<String>("apkPath") ?: return Tools.resultError()        val file = File(apkPath)        val intent = Intent(Intent.ACTION_VIEW)        //版本在7.0以上是不能直接通过uri访问的        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件            val apkUri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)            //添加这一句表示对目标应用临时授权该Uri所代表的文件            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")        } else {            intent.setDataAndType(Uri.fromFile(file),                    "application/vnd.android.package-archive")        }        activity.startActivity(intent)        return Tools.resultSuccess()    }    /**     * 获取路径文件和文件夹大小     */    fun getFilePathSize(): String? {        val filePath = call.argument<String>("filePath") ?: return null        return if (FileTools.isDirectoryExist(filePath)) {            val file = File(filePath)            if (file.isDirectory) {                FileTools.getDirectorySize(file)            } else {                FileTools.getFileSize(file)            }        } else {            "NotFile"        }    }        /**     * 判断手机是否安装某个应用     *     * @return true：安装，false：未安装 error 参数参数     */    fun isInstallApp(): Any {        val packageName = call.argument<String>("packageName") ?: Tools.resultError()        val packages: MutableList<PackageInfo> = context.packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息        for (packageInfo in packages) {            return packageName == packageInfo.packageName        }        return false    }    /**     * 跳转至应用商店     */    fun goToMarket(): String {        val packageName = call.argument<String>("packageName") ?: return Tools.resultError()        val marketPackageName = call.argument<String>("marketPackageName")                ?: return Tools.resultError()        val uri = Uri.parse("market://details?id=$packageName")        val intent = Intent(Intent.ACTION_VIEW, uri)        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)        if (marketPackageName != "") { // 如果没给市场的包名，则系统会弹出市场的列表让你进行选择。            intent.setPackage(marketPackageName)        }        activity.startActivity(intent)        return Tools.resultSuccess()    }    /**     * 拨打电话     */    fun callPhone(): String {        if (!Tools.checkPermission(Manifest.permission.CALL_PHONE)) return Tools.resultError("Lack of CALL_PHONE permissions")        val phoneNumber = call.argument<String>("phoneNumber")        val directDial = call.argument<Boolean>("directDial")        val intent = Intent()        if (directDial!!) {            intent.action = Intent.ACTION_CALL        } else {            intent.action = Intent.ACTION_DIAL        }        intent.data = Uri.parse("tel:$phoneNumber");        activity.startActivity(intent)        return Tools.resultSuccess()    }    /**     * 调用系统分享     */    fun systemShare(): String {        val type = call.argument<String>("type")        val title = call.argument<String>("title") ?: ""        val content = call.argument<String>("content")        val imagesPath = call.argument<ArrayList<String>>("imagesPath")        if (type != null) {            var shareIntent = Intent()            when {                type == "text" || type == "url" -> {                    if (content == null) {                        return Tools.resultError("not find text")                    }                    shareIntent.action = Intent.ACTION_SEND                    shareIntent.type = "text/plain"                    shareIntent.putExtra(Intent.EXTRA_TEXT, content)                }                type == "image" -> {                    if (content == null) {                        return Tools.resultError("not find image")                    }                    //将mipmap中图片转换成Uri                    val imgUri = Uri.parse(content)                    shareIntent.action = Intent.ACTION_SEND                    shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri)                    shareIntent.type = "image/*"                }                type == "images" -> {                    if (imagesPath == null) {                        return Tools.resultError("not find imagesPath")                    }                    if (imagesPath.size == 0) {                        return Tools.resultError("imagesPath size is 0")                    }                    val imgUris = ArrayList<Uri>();                    for (value in imagesPath) {                        imgUris.add(Uri.parse(value))                    }                    shareIntent.action = Intent.ACTION_SEND_MULTIPLE;                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imgUris);                    shareIntent.type = "image/*";                }            }            if (content != null || imagesPath != null) {                shareIntent = Intent.createChooser(shareIntent, title);                activity.startActivity(shareIntent)                return Tools.resultSuccess()            }            return Tools.resultError("not find text")        } else {            return Tools.resultError("not find type")        }    }}