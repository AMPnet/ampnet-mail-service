package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class NewUserWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "New wallet created"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/user-wallet-template.mustache")
    override val data: NewWalletData
        get() = NewWalletData(linkResolver.getNewWalletLink(WalletType.USER))
}

class NewOrganizationWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "New wallet created"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/organization-wallet-template.mustache")
    override val data: NewWalletData
        get() = NewWalletData(linkResolver.getNewWalletLink(WalletType.ORGANIZATION))
}

class NewProjectWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "New wallet created"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/project-wallet-template.mustache")
    override val data: NewWalletData
        get() = NewWalletData(linkResolver.getNewWalletLink(WalletType.PROJECT))
}

data class NewWalletData(val link: String)
