package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.pojo.MailListResponse
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import kotlin.jvm.Throws

@Service
class CmsServiceImpl(
    private val applicationProperties: ApplicationProperties,
    private val webClient: WebClient
) : CmsService {

    companion object : KLogging()

    @Throws(ResourceNotFoundException::class, InternalException::class)
    override fun getMail(coop: String, mailType: MailType, lang: Lang): Mono<MailListResponse> {
        val url = UriComponentsBuilder
            .fromUriString(applicationProperties.cms.baseUrl + "/mail/$coop")
            .queryParam("type", mailType)
            .queryParam("lang", lang)
            .build()
            .toUri()
        val monoResponse = webClient.get().uri(url).retrieve().bodyToMono<MailListResponse>()
        return monoResponse.doOnNext {
            if (it.mails.isEmpty())
                throw ResourceNotFoundException("Could not find translation for $mailType in $lang for coop:$coop")
        }.onErrorMap(WebClientResponseException::class.java) {
            InternalException("Could not reach headless cms service url", it)
        }
    }
}
