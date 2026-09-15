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
        val config = initService.init()
        val url = config.apkUrl
        val version = config.versionName
        Napier.i("download: $url, version: $version")
        installer.downloadAndInstall(url, version)
        return true
    }
}
