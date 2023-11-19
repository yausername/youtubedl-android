package com.yausername.youtubedl_android.domain

enum class CpuArchitecture {
    ARM32,
    ARM64,
    X86,
    X64; // x86_64

    companion object {
        fun fromString(string: String): CpuArchitecture {
            return when (string) {
                "arm32" -> ARM32
                "arm64" -> ARM64
                "x86" -> X86
                "x64" -> X64
                else -> throw IllegalArgumentException("Unknown CPU architecture: $string")
            }
        }
    }
}