package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.pojo.MailResponse

interface HeadlessCmsService {
    fun getMail(coop: String, mailType: MailType, lang: Lang): MailResponse
}
