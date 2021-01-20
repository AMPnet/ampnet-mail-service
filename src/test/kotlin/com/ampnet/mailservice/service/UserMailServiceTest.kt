package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.mailservice.proto.SuccessfullyInvestedRequest
import com.ampnet.mailservice.service.impl.UserMailServiceImpl
import com.ampnet.mailservice.service.impl.mail.toMailFormat
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import com.ampnet.userservice.proto.UserResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class UserMailServiceTest : MailServiceTestBase() {

    private val service: UserMailService by lazy {
        UserMailServiceImpl(
            mailSender, applicationProperties, linkResolverService,
            translationService, userService, projectService, walletService
        )
    }

    @Test
    fun mustSetCorrectSendConfirmationMail() {
        suppose("Service sent the mail") {
            val request = MailConfirmationRequest.newBuilder()
                .setEmail(testContext.receiverMail)
                .setLanguage("invalid")
                .setToken(testContext.token)
                .setCoop(testContext.coop)
                .build()
            service.sendConfirmationMail(request)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(confirmationMailSubject)

            val confirmationLink = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                "${applicationProperties.mail.confirmationPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            val request = ResetPasswordRequest.newBuilder()
                .setEmail(testContext.receiverMail)
                .setToken(testContext.token)
                .setCoop(testContext.coop)
                .build()
            service.sendResetPasswordMail(request)
        }

        verify("The mail is sent to right receiver and has reset password link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(resetPasswordSubject)

            val resetPasswordLink = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                "${applicationProperties.mail.resetPasswordPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(resetPasswordLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMailOnGreek() {
        suppose("Service sent reset password mail") {
            val request = ResetPasswordRequest.newBuilder()
                .setEmail(testContext.receiverMail)
                .setToken(testContext.token)
                .setCoop(testContext.coop)
                .setLanguage("el")
                .build()
            service.sendResetPasswordMail(request)
        }

        verify("The mail is on Greek and sent to right receiver with reset password link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo("Επαναφορά κωδικού πρόσβασης")

            val content = mail.mimeMessage.content.toString()
            val resetPasswordLink = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                "${applicationProperties.mail.resetPasswordPath}?token=${testContext.token}"
            assertThat(content).contains(resetPasswordLink)
            assertThat(content).contains("Εάν δεν ζήτησες αλλαγή κωδικού")
        }
    }

    @Test
    fun mustSetCorrectOrganizationInvitationMail() {
        suppose("Service sends organizationInvitation e-mails") {
            val request = OrganizationInvitationRequest.newBuilder()
                .addAllEmails(testContext.receiverEmails)
                .setSenderEmail("sender@email.com")
                .setOrganization(testContext.organizationName)
                .setCoop(testContext.coop)
                .build()
            service.sendOrganizationInvitationMail(request)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(2)
            val firstMail = mailList.first()
            val secondMail = mailList.last()
            assertThat(firstMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(firstMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.first())
            assertThat(firstMail.mimeMessage.subject).isEqualTo(invitationMailSubject)
            assertThat(secondMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.last())

            val mailText = firstMail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.organizationName)

            val link = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                applicationProperties.mail.manageOrganizationPath
            assertThat(mailText).contains(link)
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
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.amount.toMailFormat())
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
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("approved")
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
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("rejected")
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
            assertThat(mailList).hasSize(1)
            val userMail = mailList.first()

            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(withdrawSubject)
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
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("approved")
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
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("rejected")
        }
    }

    @Test
    fun mustSetCorrectSendUserWalletActivatedMail() {
        suppose("Service sent mail for user wallet activated") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid.toString())))
                .thenReturn(listOf(testContext.user))
            service.sendWalletActivatedMail(testContext.user.uuid, WalletType.USER, activationData)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                testContext.user.coop + "/" + applicationProperties.mail.walletActivatedPath
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(confirmationUserLink)
            assertThat(mailText).contains(activationData)
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
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.project.createdByUser)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sent mail for project wallet activated") {
            service.sendWalletActivatedMail(testContext.walletOwner, WalletType.PROJECT, activationData)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" + testContext.user.coop + "/" +
                applicationProperties.mail.manageOrganizationPath + "/" + testContext.project.organizationUuid +
                "/" + applicationProperties.mail.manageProjectPath + "/" + testContext.project.uuid
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(confirmationUserLink)
            assertThat(mailText).doesNotContain(activationData)
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
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.organization.createdByUser)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sent mail for organization wallet activated") {
            service.sendWalletActivatedMail(testContext.walletOwner, WalletType.ORGANIZATION, activationData)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" + testContext.user.coop + "/" +
                applicationProperties.mail.manageOrganizationPath + "/" + testContext.organization.uuid
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(confirmationUserLink)
            assertThat(mailText).doesNotContain(activationData)
        }
    }

    @Test
    fun mustNotSendOrganizationInvitationMailIfRecipientEmailIsIncorrect() {
        suppose("Service sends organizationInvitation to incorrect and correct email") {
            val correctEmail = "test@email.com"
            val incorrectEmail = "fff5555"
            val request = OrganizationInvitationRequest.newBuilder()
                .addAllEmails(listOf(correctEmail, incorrectEmail))
                .setSenderEmail("sender@email.com")
                .setOrganization(testContext.organizationName)
                .setCoop(testContext.coop)
                .build()
            service.sendOrganizationInvitationMail(request)
        }

        verify("The mail is only sent to right receiver") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
        }
    }

    @Test
    fun mustSetCorrectSendProjectFullyFundedMail() {
        suppose("Wallet service returns wallet") {
            testContext.walletHash = "wallet_hash"
            testContext.wallet = generateWalletResponse(UUID.randomUUID().toString())
            Mockito.`when`(walletService.getWalletByHash(testContext.walletHash)).thenReturn(testContext.wallet)
        }
        suppose("Project service returns project") {
            testContext.project = generateProjectResponse(UUID.randomUUID().toString())
            Mockito.`when`(projectService.getProject(UUID.fromString(testContext.wallet.owner)))
                .thenReturn(testContext.project)
        }
        suppose("User service returns user") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.project.createdByUser)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sent mail for project fully funded") {
            service.sendProjectFullyFundedMail(testContext.walletHash)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(projectFullyFundedSubject)
            val projectFullyFundedLink = applicationProperties.mail.baseUrl + "/" + testContext.user.coop + "/" +
                applicationProperties.mail.manageOrganizationPath + "/" + testContext.project.organizationUuid + "/" +
                applicationProperties.mail.manageProjectPath + "/" + testContext.project.uuid
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(projectFullyFundedLink)
        }
    }

    @Test
    fun mustSendSuccessfullyInvested() {
        suppose("Wallet service returns wallets for project and user") {
            testContext.walletFrom = generateWalletResponse(UUID.randomUUID().toString())
            testContext.walletTo = generateWalletResponse(UUID.randomUUID().toString())
            Mockito.`when`(
                walletService.getWalletsByHash(
                    setOf(testContext.walletFrom.hash, testContext.walletTo.hash)
                )
            ).thenReturn(listOf(testContext.walletFrom, testContext.walletTo))
        }
        suppose("Project service returns project") {
            testContext.project = generateProjectResponse(UUID.randomUUID().toString())
            testContext.projectWithData = ProjectWithDataResponse.newBuilder()
                .setProject(testContext.project)
                .setTosUrl("tos-url")
                .build()
            Mockito.`when`(projectService.getProjectWithData(UUID.fromString(testContext.walletTo.owner)))
                .thenReturn(testContext.projectWithData)
        }
        suppose("User service returns user") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.walletFrom.owner)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sent mail for successful funding") {
            val request = SuccessfullyInvestedRequest.newBuilder()
                .setWalletHashFrom(testContext.walletFrom.hash)
                .setWalletHashTo(testContext.walletTo.hash)
                .setAmount(testContext.amount.toString())
                .build()
            service.sendSuccessfullyInvested(request)
        }

        verify("The mail is sent to right receiver and has correct investment data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(investmentSubject)
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.projectWithData.project.name)
            assertThat(mailText).contains(testContext.projectWithData.tosUrl)
            assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }
}
