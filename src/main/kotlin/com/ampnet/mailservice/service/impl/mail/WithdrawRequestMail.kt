package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "withdraw-request-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, "Withdraw"),
        generateLanguageData(EL_LANGUAGE, templateName, "Ανάληψη")
    )

    fun setData(amount: Long): WithdrawRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }
}

class WithdrawTokenIssuerMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "token-issuer-withdrawal-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, "New withdrawal request"),
        generateLanguageData(EL_LANGUAGE, templateName, "Νέο αίτημα ανάληψης")
    )

    fun setData(user: UserResponse, amount: Long): WithdrawTokenIssuerMail {
        data = UserData(
            user.firstName, user.lastName, amount.toMailFormat(),
            linkResolver.getManageWithdrawalsLink(user.coop)
        )
        return this
    }
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
