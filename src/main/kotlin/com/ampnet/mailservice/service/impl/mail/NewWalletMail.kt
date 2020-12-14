package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val languageData: List<LanguageData> by lazy {
        val englishTemplate = when (type) {
            WalletType.USER -> DefaultMustacheFactory().compile("mustache/user-wallet-template.mustache")
            WalletType.PROJECT -> DefaultMustacheFactory().compile("mustache/project-wallet-template.mustache")
            WalletType.ORGANIZATION -> DefaultMustacheFactory().compile("mustache/organization-wallet-template.mustache")
        }
        listOf(LanguageData("en", "New wallet created", englishTemplate))
    }

    fun setData(activationData: String, coop: String): NewWalletMail {
        data = if (type == WalletType.USER) NewWalletData(linkResolver.getNewWalletLink(type, coop), activationData)
        else NewWalletData(linkResolver.getNewWalletLink(type, coop))
        return this
    }
}

data class NewWalletData(val link: String, val activationData: String? = null)
