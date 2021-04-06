package com.ampnet.mailservice.service.pojo

import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType

data class MailResponse(
    val id: Int?,
    val coop: String,
    val title: String,
    val content: String,
    val type: MailType,
    val requiredFields: List<String>,
    val lang: Lang
)

data class MailListResponse(
    val mails: List<MailResponse>
)
