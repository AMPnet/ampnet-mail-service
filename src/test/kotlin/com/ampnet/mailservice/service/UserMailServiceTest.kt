package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.impl.UserMailServiceImpl
import com.ampnet.mailservice.service.impl.mail.toMailFormat
import com.ampnet.userservice.proto.UserResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class UserMailServiceTest : MailServiceTestBase() {

    private val service: UserMailService by lazy {
        UserMailServiceImpl(mailSender, applicationProperties, linkResolverService, userService, projectService)
    }

    @Test
    fun mustSetCorrectSendConfirmationMail() {
        suppose("Service sent the mail") {
            service.sendConfirmationMail(testContext.receiverMail, testContext.token)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(confirmationMailSubject)

            val confirmationLink = applicationProperties.mail.baseUrl + "/" +
                "${applicationProperties.mail.confirmationPath}?token=${testContext.token}"
            Assertions.assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            service.sendResetPasswordMail(testContext.receiverMail, testContext.token)
        }

        verify("The mail is sent to right receiver and has reset password link") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(resetPasswordSubject)

            val resetPasswordLink = "${applicationProperties.mail.baseUrl}/" +
                "${applicationProperties.mail.resetPasswordPath}?token=${testContext.token}"
            Assertions.assertThat(mail.mimeMessage.content.toString()).contains(resetPasswordLink)
        }
    }

    @Test
    fun mustSetCorrectOrganizationInvitationMail() {
        suppose("Service sends organizationInvitation e-mails") {
            service.sendOrganizationInvitationMail(
                testContext.receiverEmails, testContext.organizationName, "sender@email.com"
            )
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(2)
            val firstMail = mailList.first()
            val secondMail = mailList.last()
            Assertions.assertThat(firstMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(firstMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.first())
            Assertions.assertThat(firstMail.mimeMessage.subject).isEqualTo(invitationMailSubject)
            Assertions.assertThat(secondMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.last())

            val mailText = firstMail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains(testContext.organizationName)

            val link = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath
            Assertions.assertThat(mailText).contains(link)
        }
    }

    @Test
    fun mustSetCorrectDepositRequestMail() {
        suppose("Service send deposit request mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendDepositRequestMail(user, testContext.amount)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }

    @Test
    fun mustSetCorrectPositiveDepositInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendDepositInfoMail(user, true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains("approved")
        }
    }

    @Test
    fun mustSetCorrectNegativeDepositInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendDepositInfoMail(user, false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains("rejected")
        }
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

        verify("The mail is sent to user and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val userMail = mailList.first()

            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(withdrawSubject)
        }
    }

    @Test
    fun mustSetCorrectPositiveWithdrawInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendWithdrawInfoMail(user, true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains("approved")
        }
    }

    @Test
    fun mustSetCorrectNegativeWithdrawInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendWithdrawInfoMail(user, false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains("rejected")
        }
    }

    @Test
    fun mustSetCorrectSendUserWalletActivatedMail() {
        suppose("Service sent mail for user wallet activated") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(user.uuid.toString())))
                .thenReturn(listOf(user))
            service.sendWalletActivatedMail(user.uuid, WalletType.USER)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink =
                applicationProperties.mail.baseUrl + "/" + applicationProperties.mail.walletActivatedPath
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test
    fun mustSetCorrectSendProjectWalletActivatedMail() {
        suppose("Project service returns project") {
            testContext.walletOwner = UUID.randomUUID().toString()
            testContext.project = generateProjectResponse(testContext.walletOwner)
            Mockito.`when`(projectService.getProject(UUID.fromString(testContext.walletOwner)))
                .thenReturn(testContext.project)
        }
        suppose("User service returns user") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.project.createdByUser)))
                .thenReturn(listOf(user))
        }
        suppose("Service sent mail for project wallet activated") {
            service.sendWalletActivatedMail(testContext.walletOwner, WalletType.PROJECT)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.project.organizationUuid +
                "/" + applicationProperties.mail.manageProjectPath + "/" + testContext.project.uuid
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test
    fun mustSetCorrectSendOrganizationWalletActivatedMail() {
        suppose("Project service returns organization") {
            testContext.walletOwner = UUID.randomUUID().toString()
            testContext.organization = generateOrganizationResponse(testContext.walletOwner)
            Mockito.`when`(projectService.getOrganization(UUID.fromString(testContext.walletOwner)))
                .thenReturn(testContext.organization)
        }
        suppose("User service returns user") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.organization.createdByUser)))
                .thenReturn(listOf(user))
        }
        suppose("Service sent mail for organization wallet activated") {
            service.sendWalletActivatedMail(testContext.walletOwner, WalletType.ORGANIZATION)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.organization.uuid
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test()
    fun mustNotSendOrganizationInvitationMailIfRecipientEmailIsIncorrect() {
        suppose("Service sends organizationInvitation to incorrect and correct email") {
            val correctEmail = "test@email.com"
            val incorrectEmail = "fff5555"
            service.sendOrganizationInvitationMail(
                listOf(correctEmail, incorrectEmail), testContext.organizationName, "sender@email.com"
            )
        }

        verify("The mail is only sent to right receiver") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
        }
    }
}
