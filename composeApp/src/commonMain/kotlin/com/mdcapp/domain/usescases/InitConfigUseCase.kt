package com.mdcapp.domain.usescases

import com.mdcapp.data.service.InitService
import com.mdcapp.domain.config.checkUpdate
import com.mdcapp.domain.entities.UpdateState
import com.mdcapp.ui.utils.AppInstaller
import io.github.aakira.napier.Napier

class InitConfigUseCase(private val initService: InitService) {
    suspend operator fun invoke(): Pair<UpdateState, String> {
        val result = initService.init()
        return checkUpdate(result)
    }

    suspend fun download(installer: AppInstaller): Boolean {
        val url = initService.init().apkUrl
        Napier.i("download: $url")
        installer.downloadAndInstall(url)
        return true
    }
}
