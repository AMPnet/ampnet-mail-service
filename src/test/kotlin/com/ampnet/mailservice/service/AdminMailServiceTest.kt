package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.impl.AdminMailServiceImpl
import com.ampnet.mailservice.service.impl.mail.toMailFormat
import com.ampnet.userservice.proto.UserResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class AdminMailServiceTest : MailServiceTestBase() {

    private val service: AdminMailService by lazy {
        AdminMailServiceImpl(mailSender, applicationProperties, linkResolverService, userService)
    }

    @Test
    fun mustSetCorrectWithdrawRequestMail() {
        suppose("User service returns a list of token issuers") {
            val tokenIssuer = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testContext.tokenIssuerMail)
                .setCoop(coop)
                .build()
            Mockito.`when`(userService.getTokenIssuers(coop))
                .thenReturn(listOf(tokenIssuer))
        }
        suppose("Service send withdraw request mail to user and token issuers") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendWithdrawRequestMail(user, testContext.amount)
        }

        verify("The mail is sent to token issuer and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val tokenIssuerMail = mailList.first()

            Assertions.assertThat(tokenIssuerMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(tokenIssuerMail.envelopeReceiver).isEqualTo(testContext.tokenIssuerMail)
            Assertions.assertThat(tokenIssuerMail.mimeMessage.subject).isEqualTo(manageWithdrawalsSubject)

            val mailText = tokenIssuerMail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }

    @Test
    fun mustSetCorrectSendNewOrganizationWalletMail() {
        suppose("Service sent mail for new organization wallet created") {
            val platformManager = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testContext.receiverMail)
                .build()
            Mockito.`when`(userService.getPlatformManagers(coop))
                .thenReturn(listOf(platformManager))
            service.sendNewWalletNotificationMail(WalletType.ORGANIZATION, coop)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/groups"
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test
    fun mustSetCorrectSendNewWalletMails() {
        suppose("Service sent mails for new user and project wallet created") {
            val platformManager = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testContext.receiverMail)
                .build()
            Mockito.`when`(userService.getPlatformManagers(coop))
                .thenReturn(listOf(platformManager))
            service.sendNewWalletNotificationMail(WalletType.USER, coop)
            service.sendNewWalletNotificationMail(WalletType.PROJECT, coop)
        }

        verify("Both mails are sent to right receivers and have confirmation link") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/user"
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)

            val projectMail = mailList[1]
            Assertions.assertThat(projectMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(projectMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(projectMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationProjectLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/project"
            Assertions.assertThat(projectMail.mimeMessage.content.toString()).contains(confirmationProjectLink)
        }
    }
}
