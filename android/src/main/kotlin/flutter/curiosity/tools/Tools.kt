package flutter.curiosity.toolsimport android.content.pm.PackageManagerimport android.database.Cursorimport android.graphics.Bitmapimport android.net.Uriimport android.os.Environmentimport android.provider.MediaStoreimport android.util.Logimport androidx.core.content.ContextCompatimport flutter.curiosity.CuriosityPlugin.Companion.contextimport io.flutter.plugin.common.MethodCallimport io.flutter.plugin.common.MethodChannelimport java.io.*object Tools {    /**     * 检测是否有权限     */    fun checkPermission(permission: String): Boolean {        return ContextCompat.checkSelfPermission(context, permission) ==                PackageManager.PERMISSION_DENIED    }    /**     * 打印日志     */    fun logInfo(content: String) {        Log.i("Curiosity--- ", content)    }    /**     * 判断Argument是否为null     */    fun isArgumentNull(key: String, call: MethodCall, result: MethodChannel.Result, function: () -> Unit) {        if (call.argument<Any>(key) == null) {            result.error("null", "$key is not null", null);            return        } else {            function()        }    }    fun resultError(error: String?): String {        return error ?: "error -- Parameter error"    }    fun resultError(): String {        return "error -- Parameter error"    }    fun resultSuccess(success: String?): String {        return success ?: "success"    }    fun resultNot(not: String?): String {        return not ?: "null"    }    fun resultSuccess(): String {        return "success"    }    fun getRealPathFromURI(contentURI: Uri?): String? {        val result: String        val cursor: Cursor = contentURI?.let { context.contentResolver.query(it, null, null, null, null) }!!        cursor.moveToFirst()        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)        result = cursor.getString(idx)        cursor.close()        return result    }    /** 保存bitmap方法  */    fun saveBitmap(bitmap: Bitmap): String? {        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), System.currentTimeMillis().toString() + ".JPEG")        if (filePath.exists()) {            filePath.delete()        }        try {            val out = FileOutputStream(filePath)            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)            out.flush()            out.close()            return filePath.absolutePath.toString()        } catch (e: FileNotFoundException) {        }        return null    }    fun bitmapToBytes(bitmap: Bitmap): ByteArray {        logInfo(bitmap.width.toString())        logInfo(bitmap.height.toString())        val out = ByteArrayOutputStream()        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)        return out.toByteArray()    }}