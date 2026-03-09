package com.mdcapp.domain.usescases

import android.util.Log
import com.mdcapp.data.service.InitService
import com.mdcapp.domain.config.checkUpdate
import com.mdcapp.domain.config.downloadInstaller
import com.mdcapp.domain.entities.UpdateState

class InitConfigUseCase(private val initService: InitService) {
    suspend operator fun invoke(): Pair<UpdateState, String> {
        val result = initService.init()
        return checkUpdate(result)
    }

    suspend fun download(context: Any): Boolean {
        val url = initService.init().apkUrl
        Log.i("MdcAppOnly", "download: $url")
        return downloadInstaller(
            context,
            url
        )
    }
}