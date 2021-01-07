package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

const val FAILED_DELIVERY_TEMPLATE = "failed-delivery-message-template.mustache"

class FailedDeliveryMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, FAILED_DELIVERY_TEMPLATE, "Email delivery failed"),
        generateLanguageData(EL_LANGUAGE, FAILED_DELIVERY_TEMPLATE, "Η παράδοση email απέτυχε")
    )

    fun setData(emails: List<String>): FailedDeliveryMail {
        FailedDeliveryRecipients(emails.joinToString { ", " })
        return this
    }
}

data class FailedDeliveryRecipients(val failedRecipients: String)
