package com.ampnet.mailservice.service.pojo

import java.io.InputStream

data class Attachment(
    val name: String,
    val file: InputStream
)
