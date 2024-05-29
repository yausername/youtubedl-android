package com.yausername.youtubedl_common.domain

enum class CpuArchitecture {
    ARM32,
    ARM64,
    X86,
    X64; // x86_64

    companion object {
        fun String.toCpuArchitecture(): CpuArchitecture {
            return when (this) {
                "armeabi-v7a" -> ARM32
                "arm64-v8a" -> ARM64
                "x86" -> X86
                "x86_64" -> X64
                else -> throw IllegalArgumentException("Unknown CPU architecture: $this")
            }
        }
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