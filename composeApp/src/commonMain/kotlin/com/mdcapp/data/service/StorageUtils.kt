package com.mdcapp.data.service

import dev.gitlive.firebase.storage.Data

expect fun wrapImageData(bytes: ByteArray): Data
