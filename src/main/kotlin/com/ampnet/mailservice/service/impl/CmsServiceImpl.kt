package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.pojo.MailListResponse
import com.ampnet.mailservice.service.pojo.MailResponse
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.util.UriComponentsBuilder

@Service
class CmsServiceImpl(
    private val restTemplate: RestTemplate,
    private val applicationProperties: ApplicationProperties
) : CmsService {

    companion object : KLogging()

    override fun getMail(coop: String, mailType: MailType, lang: Lang): MailResponse {
        val url = UriComponentsBuilder
            .fromUriString(applicationProperties.cms.baseUrl + "/mail/$coop")
            .queryParam("type", mailType)
            .queryParam("lang", lang)
            .build()
            .toUri()
        try {
            return restTemplate.getForObject<MailListResponse>(url).mails.firstOrNull()
                ?: throw ResourceNotFoundException("Could not find translation for $mailType in $lang for coop:$coop")
        } catch (ex: RestClientException) {
            throw InternalException("Could not reach headless cms service url: $url", ex)
        }
    }
}
