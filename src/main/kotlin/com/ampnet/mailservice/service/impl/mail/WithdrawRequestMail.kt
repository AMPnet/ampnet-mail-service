package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "withdrawRequestTemplate"
    override val title = "withdrawTitle"

    fun setData(amount: Long): WithdrawRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }

    fun setTemplate(language: String): WithdrawRequestMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

class WithdrawTokenIssuerMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "tokenIssuerWithdrawalTemplate"
    override val title = "newWithdrawTitle"

    fun setData(user: UserResponse, amount: Long): WithdrawTokenIssuerMail {
        data = UserData(
            user.firstName, user.lastName, amount.toMailFormat(),
            linkResolver.getManageWithdrawalsLink(user.coop)
        )
        return this
    }

    fun setTemplate(language: String): WithdrawTokenIssuerMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
