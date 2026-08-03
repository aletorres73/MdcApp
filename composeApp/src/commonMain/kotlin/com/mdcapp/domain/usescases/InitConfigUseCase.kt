package com.mdcapp.domain.usescases

import com.mdcapp.data.service.InitService
import com.mdcapp.domain.config.checkUpdate
import com.mdcapp.domain.config.downloadInstaller
import com.mdcapp.domain.entities.UpdateState
import io.github.aakira.napier.Napier

class InitConfigUseCase(private val initService: InitService) {
    suspend operator fun invoke(): Pair<UpdateState, String> {
        val result = initService.init()
        return checkUpdate(result)
    }

    suspend fun download(context: Any): Boolean {
        val url = initService.init().apkUrl
        Napier.i("download: $url")
        return downloadInstaller(
            context,
            url
        )
    }
}
