package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "withdrawRequestTemplate"
    override val titleKey = "withdrawTitle"

    fun setData(amount: Long) = apply { data = AmountData(amount.toMailFormat()) }
}

class WithdrawTokenIssuerMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "tokenIssuerWithdrawalTemplate"
    override val titleKey = "newWithdrawTitle"

    fun setData(user: UserResponse, amount: Long) = apply {
        data = UserData(
            user.firstName, user.lastName, amount.toMailFormat(),
            linkResolver.getManageWithdrawalsLink(user.coop)
        )
    }
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
