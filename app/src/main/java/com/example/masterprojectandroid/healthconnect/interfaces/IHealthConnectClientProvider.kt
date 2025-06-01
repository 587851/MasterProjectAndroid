package com.example.masterprojectandroid.healthconnect.interfaces

import android.content.Context

interface IHealthConnectClientProvider {
    fun getSdkStatus(context: Context, packageName: String): Int
}