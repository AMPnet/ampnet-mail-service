package com.ampnet.mailservice.service

import com.ampnet.mailservice.amqp.blockchainservice.SuccessfullyInvestedMessage
import com.ampnet.mailservice.amqp.projectservice.MailOrgInvitationMessage
import com.ampnet.mailservice.amqp.userservice.MailConfirmationMessage
import com.ampnet.mailservice.amqp.userservice.MailResetPasswordMessage
import com.ampnet.mailservice.amqp.walletservice.WalletTypeAmqp
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.impl.UserMailServiceImpl
import com.ampnet.mailservice.service.impl.mail.toMailFormat
import com.ampnet.mailservice.service.pojo.TERMS_OF_SERVICE
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import com.ampnet.userservice.proto.UserResponse
import org.apache.commons.mail.util.MimeMessageParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class UserMailServiceTest : MailServiceTestBase() {

    private val service: UserMailService by lazy {
        UserMailServiceImpl(
            mailSender, applicationProperties, linkResolverService, headlessCmsService,
            userService, projectService, walletService, fileService, blockchainService
        )
    }

    @Test
    fun mustSetCorrectSendConfirmationMail() {
        suppose("Service sent the mail") {
            mockHeadlessCmsServiceResponse(MailType.MAIL_CONFIRMATION_MAIL)
            val request = MailConfirmationMessage(testContext.receiverMail, testContext.token, testContext.coop, "")
            service.sendConfirmationMail(request)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val confirmationLink = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                "${applicationProperties.mail.confirmationPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            mockHeadlessCmsServiceResponse(MailType.RESET_PASSWORD_MAIL)
            val request = MailResetPasswordMessage(testContext.receiverMail, testContext.token, testContext.coop, "")
            service.sendResetPasswordMail(request)
        }

        verify("The mail is sent to right receiver and has reset password link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val resetPasswordLink = applicationProperties.mail.baseUrl + "/" + testContext.coop + "/" +
                "${applicationProperties.mail.resetPasswordPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(resetPasswordLink)
        }
    }

    @Test
    fun mustSetCorrectOrganizationInvitationMail() {
        suppose("User service will return sender user response") {
            testContext.user = generateUserResponse("sender@email.com")
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sends organizationInvitation e-mails") {
            mockHeadlessCmsServiceResponse(MailType.INVITATION_MAIL)
            val request = MailOrgInvitationMessage(
                testContext.receiverEmails,
                testContext.organizationName,
                UUID.fromString(testContext.user.uuid),
                testContext.coop
            )
            service.sendOrganizationInvitationMail(request)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(2)
            val firstMail = mailList.first()
            val secondMail = mailList.last()
            assertThat(firstMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(firstMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.first())
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
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service send deposit request mail") {
            mockHeadlessCmsServiceResponse(MailType.DEPOSIT_REQUEST_MAIL)
            service.sendDepositRequestMail(UUID.fromString(testContext.user.uuid), testContext.amount)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }

    @Test
    fun mustSetCorrectPositiveDepositInfoMail() {
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Project service returns active project") {
            testContext.project = generateProjectResponse("some user")
            Mockito.`when`(projectService.getActiveProjects(coop))
                .thenReturn(listOf(testContext.project))
        }
        suppose("Wallet service returns project wallet") {
            testContext.wallet = generateWalletResponse(testContext.project.uuid)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(UUID.fromString(testContext.wallet.owner))))
                .thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service returns project wallet balance") {
            Mockito.`when`(blockchainService.getBalance(testContext.wallet.hash))
                .thenReturn(testContext.project.expectedFunding / 2)
        }
        suppose("Service send Deposit info mail") {
            mockHeadlessCmsServiceResponse(MailType.DEPOSIT_INFO_MAIL)
            service.sendDepositInfoMail(UUID.fromString(testContext.user.uuid), true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val mailText = mail.mimeMessage.content.toString()
            val projectOffersLink = applicationProperties.mail.baseUrl + "/" +
                testContext.user.coop + "/" + applicationProperties.mail.overviewPath
            assertThat(mailText).contains(projectOffersLink)
        }
    }

    @Test
    fun mustSetCorrectPositiveDepositInfoMailWithoutProjectWhichCanReceiveInvestment() {
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Project service returns active project") {
            testContext.project = generateProjectResponse("some user")
            Mockito.`when`(projectService.getActiveProjects(coop))
                .thenReturn(listOf(testContext.project))
        }
        suppose("Wallet service will returns project wallet") {
            testContext.wallet = generateWalletResponse(testContext.project.uuid)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(UUID.fromString(testContext.wallet.owner))))
                .thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will returns project wallet balance") {
            Mockito.`when`(blockchainService.getBalance(testContext.wallet.hash))
                .thenReturn(testContext.project.expectedFunding)
        }
        suppose("Service send Deposit info mail") {
            mockHeadlessCmsServiceResponse(MailType.DEPOSIT_INFO_NO_PROJECT_TO_INVEST_MAIL)
            service.sendDepositInfoMail(UUID.fromString(testContext.user.uuid), true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val mailText = mail.mimeMessage.content.toString()
            val projectOffersLink = applicationProperties.mail.baseUrl + "/" +
                testContext.user.coop + "/" + applicationProperties.mail.overviewPath
            assertThat(mailText).doesNotContain(projectOffersLink)
        }
    }

    @Test
    fun mustSetCorrectNegativeDepositInfoMail() {
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service send Deposit info mail") {
            mockHeadlessCmsServiceResponse(MailType.DEPOSIT_FAILED_INFO_MAIL)
            service.sendDepositInfoMail(UUID.fromString(testContext.user.uuid), false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)

            val mailText = mail.mimeMessage.content.toString()
            val projectOffersLink = applicationProperties.mail.baseUrl + "/" +
                testContext.user.coop + "/" + applicationProperties.mail.overviewPath
            assertThat(mailText).doesNotContain(projectOffersLink)
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
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service send withdraw request mail to user and token issuers") {
            mockHeadlessCmsServiceResponse(MailType.WITHDRAW_REQUEST_MAIL)
            service.sendWithdrawRequestMail(UUID.fromString(testContext.user.uuid), testContext.amount)
        }

        verify("The mail is sent to user and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val userMail = mailList.first()

            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
        }
    }

    @Test
    fun mustSetCorrectPositiveWithdrawInfoMail() {
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service send Deposit info mail") {
            mockHeadlessCmsServiceResponse(MailType.WITHDRAW_INFO_MAIL)
            service.sendWithdrawInfoMail(UUID.fromString(testContext.user.uuid), true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
        }
    }

    @Test
    fun mustSetCorrectNegativeWithdrawInfoMail() {
        suppose("User service will return user response") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service send Deposit info mail") {
            mockHeadlessCmsServiceResponse(MailType.WITHDRAW_FAILED_INFO_MAIL)
            service.sendWithdrawInfoMail(UUID.fromString(testContext.user.uuid), false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
        }
    }

    @Test
    fun mustSetCorrectSendUserWalletActivatedMail() {
        suppose("Service sent mail for user wallet activated") {
            testContext.user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid.toString())))
                .thenReturn(listOf(testContext.user))
            mockHeadlessCmsServiceResponse(MailType.ACTIVATED_USER_WALLET_MAIL)
            service.sendWalletActivatedMail(UUID.fromString(testContext.user.uuid), WalletTypeAmqp.USER, activationData)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
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
            mockHeadlessCmsServiceResponse(MailType.ACTIVATED_PROJECT_WALLET_MAIL)
            service.sendWalletActivatedMail(
                UUID.fromString(testContext.walletOwner), WalletTypeAmqp.PROJECT, activationData
            )
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
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
            mockHeadlessCmsServiceResponse(MailType.ACTIVATED_ORGANIZATION_WALLET_MAIL)
            service.sendWalletActivatedMail(
                UUID.fromString(testContext.walletOwner), WalletTypeAmqp.ORGANIZATION, activationData
            )
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" + testContext.user.coop + "/" +
                applicationProperties.mail.manageOrganizationPath + "/" + testContext.organization.uuid
            val mailText = userMail.mimeMessage.content.toString()
            assertThat(mailText).contains(confirmationUserLink)
            assertThat(mailText).doesNotContain(activationData)
        }
    }

    @Test
    fun mustNotSendOrganizationInvitationMailIfRecipientEmailIsIncorrect() {
        suppose("User service will return sender user response") {
            testContext.user = generateUserResponse("sender@email.com")
            Mockito.`when`(userService.getUsers(listOf(testContext.user.uuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Service sends organizationInvitation to incorrect and correct email") {
            mockHeadlessCmsServiceResponse(MailType.INVITATION_MAIL)
            val correctEmail = "test@email.com"
            val incorrectEmail = "fff5555"
            val request = MailOrgInvitationMessage(
                listOf(correctEmail, incorrectEmail),
                testContext.organizationName,
                UUID.fromString(testContext.user.uuid),
                testContext.coop
            )
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
            mockHeadlessCmsServiceResponse(MailType.PROJECT_FULLY_FUNDED_MAIL)
            service.sendProjectFullyFundedMail(testContext.walletHash)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
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
                .setTosUrl(testContext.tosUrl)
                .build()
            Mockito.`when`(projectService.getProjectWithData(UUID.fromString(testContext.walletTo.owner)))
                .thenReturn(testContext.projectWithData)
        }
        suppose("User service returns user with info") {
            testContext.userWithInfo = generateUserWithInfoResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUserWithInfo(testContext.walletFrom.owner))
                .thenReturn(testContext.userWithInfo)
        }
        suppose("File service returns pdf file") {
            val termsOfService = javaClass.classLoader.getResource("test.pdf")?.readBytes()
            Mockito.`when`(fileService.getTermsOfService(testContext.tosUrl)).thenReturn(termsOfService)
        }
        suppose("Service sent mail for successful funding") {
            mockHeadlessCmsServiceResponse(MailType.SUCCESSFULLY_INVESTED_MAIL, coop)
            val message = SuccessfullyInvestedMessage(
                testContext.walletFrom.hash, testContext.walletTo.hash, testContext.amount.toString()
            )
            service.sendSuccessfullyInvested(message)
        }

        verify("The mail is sent to right receiver and has correct investment data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            val mimeMessageParser = MimeMessageParser(userMail.mimeMessage).parse()
            assertThat(mimeMessageParser.hasAttachments()).isTrue
            val mailText = mimeMessageParser.htmlContent
            val attachment = mimeMessageParser.findAttachmentByName(TERMS_OF_SERVICE)
            verifyPdfFormat(getFileContent(attachment.inputStream))
            assertThat(mailText).contains(testContext.projectWithData.project.name)
            assertThat(mailText).contains(testContext.projectWithData.project.description)
            assertThat(mailText).contains(testContext.userWithInfo.coop.name)
            assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }

    @Test
    fun mustSendSuccessfullyInvestedWithoutAttachmentIfTosIsBlank() {
        suppose("Wallet service returns wallets for project and user") {
            testContext.walletFrom = generateWalletResponse(UUID.randomUUID().toString())
            testContext.walletTo = generateWalletResponse(UUID.randomUUID().toString())
            Mockito.`when`(
                walletService.getWalletsByHash(
                    setOf(testContext.walletFrom.hash, testContext.walletTo.hash)
                )
            ).thenReturn(listOf(testContext.walletFrom, testContext.walletTo))
        }
        suppose("Project service returns project without tos") {
            testContext.project = generateProjectResponse(UUID.randomUUID().toString())
            testContext.projectWithData = ProjectWithDataResponse.newBuilder()
                .setProject(testContext.project)
                .build()
            Mockito.`when`(projectService.getProjectWithData(UUID.fromString(testContext.walletTo.owner)))
                .thenReturn(testContext.projectWithData)
        }
        suppose("User service returns user with info") {
            testContext.userWithInfo = generateUserWithInfoResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUserWithInfo(testContext.walletFrom.owner))
                .thenReturn(testContext.userWithInfo)
        }
        suppose("Service sent mail for successful funding") {
            mockHeadlessCmsServiceResponse(MailType.SUCCESSFULLY_INVESTED_WITHOUT_TOS_MAIL, coop)
            val message = SuccessfullyInvestedMessage(
                testContext.walletFrom.hash, testContext.walletTo.hash, testContext.amount.toString()
            )
            service.sendSuccessfullyInvested(message)
        }

        verify("The mail is sent to right receiver with right data and has no attachment") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            val mimeMessageParser = MimeMessageParser(userMail.mimeMessage).parse()
            assertThat(mimeMessageParser.hasAttachments()).isFalse
            val mailText = mimeMessageParser.htmlContent
            assertThat(mailText).contains(testContext.projectWithData.project.name)
            assertThat(mailText).contains(testContext.projectWithData.project.description)
            assertThat(mailText).contains(testContext.userWithInfo.coop.name)
            assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
    }

    private fun getFileContent(inputStream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return outputStream.toByteArray()
    }
}
