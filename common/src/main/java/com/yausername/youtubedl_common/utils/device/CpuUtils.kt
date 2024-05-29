package com.yausername.youtubedl_common.utils.device

import android.os.Build
import com.yausername.youtubedl_common.domain.CpuArchitecture
import com.yausername.youtubedl_common.domain.CpuArchitecture.Companion.toCpuArchitecture

object CpuUtils {
    val abiList = Build.SUPPORTED_ABIS
    fun getPreferredAbi(): CpuArchitecture {
        return abiList.firstOrNull()?.toCpuArchitecture() ?: throw IllegalStateException("No supported ABI found")
    }
}