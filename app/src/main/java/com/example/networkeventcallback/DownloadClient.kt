/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File


interface DownloadClient {

    fun startDownload()

    fun resumeDownload()

    fun cancelDownload()

    fun pauseDownload()

    fun cancelOnGoingDownloadAndDeleteFile()

    val downloadDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    val BUFFER_SIZE: Int
        get() = 4096


    data class Builder(
        private var downloadUrl: String? = null,
        private var destinationFile: File? = null,
        private var downloadCallback: DownloadCallback? = null,
        private var fileName: String? = null,
        private var authToken: String = "",
        private var downloadLengthUnit: DownloadLengthUnit = DownloadLengthUnit.NONE
    ) {
        fun addDownloadUrl(url: String) = apply {
            this.downloadUrl = url
        }

        fun addDownloadFileDirectory(downloadFile: File) = apply {
            this.destinationFile = downloadFile
        }

        fun addDownloadCallback(callback: DownloadCallback) = apply {
            this.downloadCallback = callback
        }

        fun addFileName(fileName: String?) = apply {
            this.fileName = fileName
        }

        fun addDownloadLengthUnit(downloadLengthUnit: DownloadLengthUnit) = apply {
            this.downloadLengthUnit = downloadLengthUnit
        }

        fun addAuthToken(authToken: String) = apply {
            this.authToken = authToken
        }

        fun build(): DownloadClient {
            return when {
                downloadUrl.isNullOrEmpty() -> throw Exception("Provide valid url")
                destinationFile == null -> throw Exception("Provide valid destination file")
                downloadCallback == null -> throw Exception("Provide necessary callback to observe download state")
                isFileExtensionInvalid() -> throw Exception("Provide valid extension type e.g: (file.txt)")
                else -> FileDownloadManager(downloadUrl, destinationFile, downloadCallback, fileName, authToken, downloadLengthUnit)
            }
        }

        private fun isFileExtensionInvalid(): Boolean {
            return fileName?.substringAfter(".")?.length == 0
        }
    }
}

data class ProgressData(
    var fileName: String = "",
    var progress: Double = 0.0,
    var downloadTillLength: String = "",
    var totalFileLength: String = "",
    var isFileDeleted: Boolean = false
)

interface DownloadCallback {
    fun onSuccess(downloadedFile: File)
    fun onFailure(failureReason: String)
    fun onProgressUpdate(progressData: ProgressData)
}