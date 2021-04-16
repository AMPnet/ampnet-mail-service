package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.pojo.MailListResponse
import reactor.core.publisher.Mono

interface CmsService {
    fun getMail(coop: String, mailType: MailType, lang: Lang): Mono<MailListResponse>
}
