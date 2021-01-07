package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val languageData: List<LanguageData> by lazy {
        val templateName = when (type) {
            WalletType.USER -> "user-wallet-template.mustache"
            WalletType.PROJECT -> "project-wallet-template.mustache"
            WalletType.ORGANIZATION ->
                "organization-wallet-template.mustache"
        }
        listOf(
            generateLanguageData(EN_LANGUAGE, templateName, "New wallet created"),
            generateLanguageData(EL_LANGUAGE, templateName, "Δημιουργήθηκε νέο πορτοφόλι")
        )
    }

    fun setData(activationData: String, coop: String): NewWalletMail {
        data = if (type == WalletType.USER) NewWalletData(linkResolver.getNewWalletLink(type, coop), activationData)
        else NewWalletData(linkResolver.getNewWalletLink(type, coop))
        return this
    }
}

data class NewWalletData(val link: String, val activationData: String? = null)
