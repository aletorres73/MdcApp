package com.mdcapp.data.service

import dev.gitlive.firebase.storage.Data

actual fun wrapImageData(bytes: ByteArray): Data = Data(bytes)
