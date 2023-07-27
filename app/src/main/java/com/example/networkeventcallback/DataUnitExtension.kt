/**
 * Copyright 2023 Lenovo, All Rights Reserved *
 */
package com.example.networkeventcallback

import java.util.Locale

const val KILOBYTES = 1000.00
const val MEGABYTES = 1000000.00
const val GIGABYTES = 1000000000.00
const val TERABYTES = 1000000000000.00
const val PETABYTES = 999999999999999.9
const val EXABYTES = 1000000000000000000.00
const val COMPARE_UNIT = 1000.00
const val DOWNLOAD_UNIT_FORMAT = "%.1f %s"

fun Long.toBytes(): Long {
    return this
}

fun Long.toKilobytes(): Double {
    return (this.toDouble() / KILOBYTES)
}

fun Long.toMegabytes(): Double {
    return (this.toDouble() / MEGABYTES)
}

fun Long.toGigabytes(): Double {
    return (this.toDouble() / GIGABYTES)
}

fun Long.toTerabytes(): Double {
    return (this.toDouble() / TERABYTES)
}

fun Long.toPetabytes(): Double {
    return (this.toDouble() / PETABYTES)
}

fun Long.toExabytes(): Double {
    return (this.toDouble() / EXABYTES)
}

fun Double.formatSizeUnit(unit: String): String {
    return String.format(Locale.ENGLISH, DOWNLOAD_UNIT_FORMAT, this, unit)
}

fun Long.getDownloadUnit(downloadLengthUnit: DownloadLengthUnit): String {
    if (this <= 0) {
        return ""
    }
    return when(downloadLengthUnit) {
        DownloadLengthUnit.BYTES -> this.toBytes().toString().plus(" ${downloadLengthUnit.value}")
        DownloadLengthUnit.KB -> this.toKilobytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.MB -> this.toMegabytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.GB -> this.toGigabytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.TB -> this.toTerabytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.PB -> this.toPetabytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.EB -> this.toExabytes().formatSizeUnit(downloadLengthUnit.value)
        DownloadLengthUnit.NONE -> {
            val bytes = this.toBytes()
            val kb = this.toKilobytes()
            val mb = this.toMegabytes()
            val gb = this.toGigabytes()
            val tb = this.toTerabytes()
            val pb = this.toPetabytes()
            val eb = this.toExabytes()
            when {
                bytes <= COMPARE_UNIT.toInt() -> bytes.toString().plus(" ${DownloadLengthUnit.BYTES.value}")
                kb <= COMPARE_UNIT -> kb.formatSizeUnit(DownloadLengthUnit.KB.value)
                mb <= COMPARE_UNIT -> mb.formatSizeUnit(DownloadLengthUnit.MB.value)
                gb <= COMPARE_UNIT -> gb.formatSizeUnit(DownloadLengthUnit.GB.value)
                tb <= COMPARE_UNIT -> tb.formatSizeUnit(DownloadLengthUnit.TB.value)
                pb <= COMPARE_UNIT -> pb.formatSizeUnit(DownloadLengthUnit.PB.value)
                eb <= COMPARE_UNIT -> eb.formatSizeUnit(DownloadLengthUnit.EB.value)
                else -> ""
            }
        }
    }
}

enum class DownloadLengthUnit(val value: String) {
    BYTES("Bytes"),
    KB("KB"),
    MB("MB"),
    GB("GB"),
    TB("TB"),
    PB("PB"),
    EB("EB"),
    NONE("None")
}


/*
private fun getDownloadUnit(size: Long): String {
    if (size <= 0) {
        return ""
    }
    val units = arrayOf(
        DownloadLengthUnit.BYTES.value,
        DownloadLengthUnit.KB.value,
        DownloadLengthUnit.MB.value,
        DownloadLengthUnit.GB.value,
        DownloadLengthUnit.TB.value
    )
    val digitGroups = if (downloadLengthUnit == DownloadLengthUnit.NONE) {
        (log10(size.toDouble()) / log10(1024.0)).toInt()
    } else {
        downloadLengthUnit.index
    }
    return ("${DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))} ${units[digitGroups]}")
}*/
