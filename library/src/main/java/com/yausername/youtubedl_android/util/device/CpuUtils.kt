package com.yausername.youtubedl_android.util.device

import android.os.Build
import com.yausername.youtubedl_android.domain.CpuArchitecture
import com.yausername.youtubedl_android.domain.CpuArchitecture.Companion.fromCompleteAbiToCpuArchitecture

object CpuUtils {
    val abiList = Build.SUPPORTED_ABIS
    fun getPreferredAbi(): CpuArchitecture {
        return abiList.firstOrNull()?.fromCompleteAbiToCpuArchitecture() ?: throw IllegalStateException("No supported ABI found")
    }
}