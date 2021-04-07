package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = MailType.WITHDRAW_REQUEST_MAIL

    fun setTemplateData(amount: Long) = apply { templateData = AmountData(amount.toMailFormat()) }
}

class WithdrawTokenIssuerMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = MailType.TOKEN_ISSUER_WITHDRAWAL_REQUEST_MAIL

    fun setTemplateData(user: UserResponse, amount: Long) = apply {
        this.coop = user.coop
        templateData = UserData(
            user.firstName, user.lastName, amount.toMailFormat(),
            linkResolver.getManageWithdrawalsLink(user.coop)
        )
    }
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
