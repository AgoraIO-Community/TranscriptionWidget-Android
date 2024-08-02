package io.agora.transcription_widget.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.agora.transcription_widget.internal.utils.LogUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.UUID
import kotlin.math.pow

object Utils {
    @JvmStatic
    fun hideSoftInput(view: View?) {
        if (view == null) {
            return
        }
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun getRandomString(length: Int): String {
        val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuffer()
        for (i in 0 until length) {
            val number = random.nextInt(62)
            sb.append(str[number])
        }
        return sb.toString()
    }

    @JvmStatic
    fun getUuid(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    @JvmStatic
    fun getFromAssets(context: Context?, fileName: String?): String {
        if (null == context || TextUtils.isEmpty(fileName)) {
            return ""
        }
        try {
            val result = StringBuilder()
            try {
                InputStreamReader(fileName?.let { context.resources.assets.open(it) }).use { inputReader ->
                    BufferedReader(inputReader).use { bufReader ->
                        var line: String?
                        while (bufReader.readLine().also { line = it } != null) {
                            result.append(line)
                        }
                        return result.toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 拷贝asset目录下所有文件到指定路径
     *
     * @param context    context
     * @param assetsPath asset目录
     * @param savePath   目标目录
     */
    @JvmStatic
    fun copyFilesFromAssets(context: Context, assetsPath: String, savePath: String) {
        try {
            // 获取assets指定目录下的所有文件
            val fileList = context.assets.list(assetsPath)
            if (fileList != null && fileList.size > 0) {
                val file = File(savePath)
                // 如果目标路径文件夹不存在，则创建
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return
                    }
                }
                for (fileName in fileList) {
                    copyFileFromAssets(context, "$assetsPath/$fileName", savePath, fileName)
                }
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    /**
     * 拷贝asset文件到指定路径，可变更文件名
     *
     * @param context   context
     * @param assetName asset文件
     * @param savePath  目标路径
     * @param saveName  目标文件名
     */
    @JvmStatic
    fun copyFileFromAssets(
        context: Context,
        assetName: String?,
        savePath: String,
        saveName: String
    ) {
        // 若目标文件夹不存在，则创建
        val dir = File(savePath)
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                return
            }
        }

        // 拷贝文件
        val filename = "$savePath/$saveName"
        val file = File(filename)
        if (!file.exists()) {
            try {
                if (assetName != null) {
                    context.assets.open(assetName).use { inStream ->
                        FileOutputStream(filename).use { fileOutputStream ->
                            var byteread: Int
                            val buffer = ByteArray(1024)
                            while (inStream.read(buffer).also { byteread = it } != -1) {
                                fileOutputStream.write(buffer, 0, byteread)
                            }
                            fileOutputStream.flush()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun clearDirectory(directoryPath: String) {
        if (TextUtils.isEmpty(directoryPath)) {
            return
        }
        try {
            val directory = File(directoryPath)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                if (files != null) {
                    for (file in files) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun deleteFolder(folderPath: String) {
        if (folderPath.isEmpty()) {
            return
        }
        val folder = File(folderPath)

        // 如果文件夹存在
        if (folder.exists()) {
            val files = folder.listFiles() // 获取文件夹下的所有文件和文件夹
            if (files != null) {
                // 删除文件夹下所有的文件和文件夹
                for (file in files) {
                    if (file.isFile) {
                        file.delete() // 如果是文件，直接删除
                    } else {
                        deleteFolder(file.absolutePath) // 如果是文件夹，递归调用删除文件夹方法
                    }
                }
            }
            folder.delete() // 删除文件夹本身
        }
    }

    @JvmStatic
    fun deleteFile(filePath: String) {
        if (filePath.isEmpty()) {
            return
        }
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    @JvmStatic
    fun readFileToString(filePath: String): String {
        if (filePath.isEmpty()) {
            return ""
        }
        val file = File(filePath)
        if (!file.exists()) {
            return ""
        }
        val stringBuilder = StringBuilder()
        try {
            BufferedReader(FileReader(filePath)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
            }
        } catch (e: Exception) {
            LogUtils.e("readFileToString: " + e.message)
        }
        return stringBuilder.toString()
    }

    @JvmStatic
    fun saveTextToFile(text: String, filePath: String, isAppend: Boolean) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            try {
                BufferedWriter(FileWriter(filePath, isAppend)).use { writer -> writer.write(text) }
            } catch (e: Exception) {
                LogUtils.d("saveTextToFile: " + e.message)
            }
        } catch (e: Exception) {
            LogUtils.e("saveTextToFile: " + e.message)
        }
    }

    @JvmStatic
    fun hash64(str: String): Long {
        val bytes = str.toByteArray()

        val seed: Long = 0xc70f6907UL.toLong() // 任意设定一个种子

        val m: Long = 0xc6a4a793UL.toLong()
        var h: Long = seed xor (bytes.size.toLong() * m)

        var remainingBytes = bytes.size
        var currentOffset = 0

        while (remainingBytes >= 8) {
            var k = ByteBuffer.wrap(bytes, currentOffset, 8).order(ByteOrder.LITTLE_ENDIAN).long
            k *= m
            k = k xor (k ushr 47)
            k *= m
            h = h xor k
            h *= m

            currentOffset += 8
            remainingBytes -= 8
        }

        if (remainingBytes > 0) {
            val finalBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until remainingBytes) {
                finalBytes.put(bytes[currentOffset + i])
            }

            var k = finalBytes.long
            k *= m
            k = k xor (k ushr 47)
            k *= m
            h = h xor k
        }

        h *= m
        h = h xor (h ushr 47)
        h *= m

        return h
    }

    @JvmStatic
    fun getIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && (address.hostAddress?.indexOf(':')
                            ?: 1) < 0
                    ) {
                        return address.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    @JvmStatic
    fun getCurrentTimestampSeconds(): Int {
        return (System.currentTimeMillis() / 1000).toInt()
    }

    @JvmStatic
    fun getCurrentDateStr(pattern: String?): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date())
    }

    @JvmStatic
    fun adjustPcmDataVolume(data: ByteArray?, volume: Int): ByteArray? {
        if (null == data) {
            return null
        }
        val factor = 10.0.pow((volume.toDouble() / 20))
        val data2 = ByteArray(data.size)
        var nCur = 0
        while (nCur < data.size) {
            var volum = (data[nCur].toInt() and 0xFF or (data[nCur + 1].toInt() shl 8)).toShort()
            volum = (volum * factor).toInt().toShort()
            data2[nCur] = (volum.toInt() and 0xFF).toByte()
            data2[nCur + 1] = (volum.toInt() shr 8 and 0xFF).toByte()
            nCur += 2
        }
        return data2
    }

    @JvmStatic
    fun copyNewByteBuffer(src: ByteBuffer): ByteBuffer {
        // 确保原始ByteBuffer是可读的
        src.rewind()
        // 创建一个新的ByteBuffer，容量至少与原始ByteBuffer相同
        val newByteBuffer = ByteBuffer.allocate(src.remaining())
        // 从原始ByteBuffer读取数据到新的ByteBuffer
        newByteBuffer.put(src)
        // 切换到读模式
        newByteBuffer.flip()
        return newByteBuffer
    }

    @JvmStatic
    fun dpToPx(dp: Int, context: Context): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    @JvmStatic
    fun setRoundedBackground(view: View, backgroundColor: Int, cornerRadius: Float) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(backgroundColor)
        shape.cornerRadius = cornerRadius

        view.background = shape
    }

}
