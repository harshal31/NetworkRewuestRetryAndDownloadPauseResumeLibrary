/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Paths
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.name
import kotlin.math.round

class FileDownloadManager(
    private var downloadUrl: String? = null,
    private var destinationFile: File? = null,
    private var downloadCallback: DownloadCallback? = null,
    private val fileName: String? = null,
    private val authToken: String = "",
    private var downloadLengthUnit: DownloadLengthUnit = DownloadLengthUnit.NONE
) : DownloadClient {

    private val TAG = FileDownloadManager::class.java.simpleName
    private var downloadScope: CoroutineScope? = null
    private var job: Job? = null
    private var mPosition: Long = 0L
    private var mTotal: Long = 0L
    private var file: File? = null
    private val downloadProgress = ProgressData()

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun startDownload() {
        Log.d(TAG, "Starting download")
        initializedCoroutineScopeOnlyOnce()
        job = downloadScope?.launch(downloadDispatcher) {
            val path = File(destinationFile?.absolutePath ?: "")
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    downloadCallback?.onFailure("Not able to create file")
                    return@launch
                }
            }

            file = destinationFile.createFileWithNameIfDirectoryElseReturnFile()
            val saveFilePath = file?.absolutePath ?: ""
            if (file == null) {
                downloadCallback?.onFailure("Not able to create file")
                return@launch
            }
            updateDownloadProgress()
            var httpConn: HttpsURLConnection? = null
            var inputStream: InputStream? = null
            var randomAccessFile: RandomAccessFile? = null
            val contentLength: Long
            try {
                val url = URL(downloadUrl)
                httpConn = url.openConnection() as HttpsURLConnection
                httpConn.setRequestProperty("Connection", "Keep-Alive")

                if (authToken.isNotEmpty()) {
                    httpConn.setRequestProperty("Authorization", "Bearer $authToken")
                }

                if (mPosition != 0L) {
                    // seek to last download position,create PARTIAL download
                    httpConn.setRequestProperty("Range", "bytes=$mPosition-")
                } else if (file?.length() != 0L && mPosition == 0L) {
                    // If file is partially download previously and due to some reason our app is closed so download will interrupt in between
                    // To handle this scenario we check if file.length() not zero and mPosition is zero then tak
                    mPosition = file?.length() ?: 0L
                    httpConn.setRequestProperty("Range", "bytes=$mPosition-")
                    mTotal = mPosition + httpConn.contentLengthLong
                }

                val responseCode = httpConn.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    if (isFileDownloadCompleted()) {
                        downloadCallback?.onFailure("File with ${file?.name} is already downloaded")
                        Log.d(TAG, "File with ${file?.name} is already downloaded")
                    } else {
                        downloadCallback?.onFailure("Fail to communicate with server with $responseCode response")
                        Log.d(TAG, "Fail to communicate with server with $responseCode response")
                    }
                } else {
                    val disposition = httpConn.getHeaderField("Content-Disposition")
                    val contentType = httpConn.contentType
                    contentLength = httpConn.contentLengthLong

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // full download
                        mPosition = 0
                        mTotal = contentLength
                    }

                    Log.d(TAG, disposition + contentType)
                    inputStream = httpConn.inputStream
                    var bytesRead: Int
                    val buffer = ByteArray(BUFFER_SIZE)
                    randomAccessFile = RandomAccessFile(saveFilePath, "rw")
                    randomAccessFile.seek(mPosition)

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (!isActive) {
                            break
                        }
                        randomAccessFile.write(buffer, 0, bytesRead)
                        mPosition += bytesRead.toLong()
                        updateDownloadProgress((mPosition.toDouble() / mTotal.toDouble() * 100).roundPrecise(2))
                        downloadCallback?.onProgressUpdate(downloadProgress)
                    }

                    randomAccessFile.fd.sync()
                    if (isActive) {
                        downloadCallback?.onSuccess(file!!)
                    } else {
                        downloadCallback?.onFailure("File download is stopped")
                    }
                    cancelOnGoingCoroutine()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                downloadCallback?.onFailure("File download failed with ${e.localizedMessage ?: e.message ?: e.stackTraceToString()}")
            } finally {
                runCatching {
                    randomAccessFile?.fd?.sync()
                }
                randomAccessFile?.closeQuietly()
                inputStream?.closeQuietly()
                httpConn?.disconnect()
                if (isFileDownloadCompleted().not()) {
                    downloadCallback?.onFailure("Issue when try to download file")
                }
            }
        }
    }

    override fun resumeDownload() {
        if (downloadScope == null) {
            Log.d(TAG, "No file download is started yet")
            return
        }

        if (isFileDownloadCompleted()) {
            Log.d(TAG, "File is already downloaded")
            return
        }

        if (job?.isActive == true) {
            Log.d(TAG, "File download is already in progress")
            return
        }

        Log.d(TAG, "Resuming download")
        startDownload()
    }

    override fun pauseDownload() {
        Log.d(TAG, "Pausing Download")
        cancelOnGoingCoroutine()
    }

    override fun cancelDownload() {
        Log.d(TAG, "Cancelling Download")
        cancelOnGoingCoroutine()
    }

    override fun cancelOnGoingDownloadAndDeleteFile() {
        Log.d(TAG, "Cancelling and deleting downloading file")
        cancelOnGoingCoroutine()
        if (file.deleteSpecifiedFile()) {
            updateDownloadProgress(isFileDeleted = true)
            downloadCallback?.onProgressUpdate(downloadProgress)
        }
    }

    private fun cancelOnGoingCoroutine() {
        job?.cancel()
    }

    private fun initializedCoroutineScopeOnlyOnce() {
        job = null
        if (downloadScope == null) {
            downloadScope = CoroutineScope(downloadDispatcher)
        }
    }

    private fun getFileNameFromUri(url: String?): String {
        return try {
            Paths.get(URI.create(url).path).fileName.name
        } catch (e: Exception) {
            ""
        }
    }

    private fun File?.createFileWithNameIfDirectoryElseReturnFile(): File? {
        return try {
            if (this?.isDirectory == true) {
                val saveFilePath = this.absolutePath + File.separator + (fileName ?: getFileNameFromUri(downloadUrl))
                File(saveFilePath)
            } else {
                this
            }
        } catch (e: Exception) {
            Log.d(TAG, "exception occur while creating file ${e.message ?: e.localizedMessage ?: e.stackTraceToString()}")
            null
        }
    }

    private fun File?.deleteSpecifiedFile(): Boolean {
        return try {
            this?.delete() ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun updateDownloadProgress(progress: Double = 0.0, isFileDeleted: Boolean = false) {
        downloadProgress.fileName = if (isFileDeleted) "" else (file?.name ?: "")
        downloadProgress.progress = if (isFileDeleted) 0.0 else progress
        downloadProgress.totalFileLength = if (isFileDeleted) "" else mTotal.getDownloadUnit(downloadLengthUnit)
        downloadProgress.downloadTillLength = if (isFileDeleted) "" else mPosition.getDownloadUnit(downloadLengthUnit)
        downloadProgress.isFileDeleted = isFileDeleted
        resetAllFilePropertiesIfFileIsDeleted(isFileDeleted)
    }

    private fun resetAllFilePropertiesIfFileIsDeleted(isFileDeleted: Boolean = false) {
        if (isFileDeleted) {
            mPosition = 0
            mTotal = 0
            file = null
        }
    }

    private fun Double.roundPrecise(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun isFileDownloadCompleted() = file?.length() == mTotal
}