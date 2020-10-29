package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.impl.MailServiceImpl
import com.ampnet.mailservice.service.mail.toMailFormat
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.userservice.proto.UserResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.subethamail.wiser.Wiser
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest
@EnableConfigurationProperties
@Import(JavaMailSenderImpl::class)
class MailServiceTest : TestBase() {

    @Autowired
    private lateinit var mailSender: JavaMailSenderImpl

    @Autowired
    private lateinit var applicationProperties: ApplicationProperties

    private val userService = Mockito.mock(UserService::class.java)
    private val projectService = Mockito.mock(ProjectService::class.java)

    private lateinit var service: MailServiceImpl
    private lateinit var wiser: Wiser
    private var defaultMailPort: Int = 0
    private val testContext = TestContext()
    private val coop = "ampnet-test"
    private val confirmationMailSubject = "Confirm your email"
    private val resetPasswordSubject = "Reset password"
    private val invitationMailSubject = "Invitation"
    private val depositSubject = "Deposit"
    private val withdrawSubject = "Withdraw"
    private val newWalletSubject = "New wallet created"
    private val manageWithdrawalsSubject = "New withdrawal request"
    private val walletActivatedSubject = "Wallet activated"

    @BeforeEach
    fun init() {
        defaultMailPort = mailSender.port
        wiser = Wiser(0)
        wiser.start()
        mailSender.port = wiser.server.port
        service = MailServiceImpl(
            mailSender, applicationProperties, userService, projectService
        )
    }

    @AfterEach
    fun tearDown() {
        wiser.stop()
        mailSender.port = defaultMailPort
    }

    @Test
    fun mustSetCorrectSendConfirmationMail() {
        suppose("Service sent the mail") {
            service.sendConfirmationMail(testContext.receiverMail, testContext.token)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(confirmationMailSubject)

            val confirmationLink = applicationProperties.mail.baseUrl + "/" +
                "${applicationProperties.mail.confirmationPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            service.sendResetPasswordMail(testContext.receiverMail, testContext.token)
        }

        verify("The mail is sent to right receiver and has reset password link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(resetPasswordSubject)

            val resetPasswordLink = "${applicationProperties.mail.baseUrl}/" +
                "${applicationProperties.mail.resetPasswordPath}?token=${testContext.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(resetPasswordLink)
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
            assertThat(mailList).hasSize(2)
            val firstMail = mailList.first()
            val secondMail = mailList.last()
            assertThat(firstMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(firstMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.first())
            assertThat(firstMail.mimeMessage.subject).isEqualTo(invitationMailSubject)
            assertThat(secondMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.last())

            val mailText = firstMail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.organizationName)

            val link = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath
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

        verify("The mail is sent to token issuer and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(2)
            val tokenIssuerMail = mailList.first()

            assertThat(tokenIssuerMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(tokenIssuerMail.envelopeReceiver).isEqualTo(testContext.tokenIssuerMail)
            assertThat(tokenIssuerMail.mimeMessage.subject).isEqualTo(manageWithdrawalsSubject)

            val mailText = tokenIssuerMail.mimeMessage.content.toString()
            assertThat(mailText).contains(testContext.amount.toMailFormat())
        }
        verify("The mail is sent to user and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList[1]

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
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/user"
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)

            val projectMail = mailList[1]
            assertThat(projectMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(projectMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(projectMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationProjectLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/project"
            assertThat(projectMail.mimeMessage.content.toString()).contains(confirmationProjectLink)
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
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletPath + "/groups"
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
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
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" + applicationProperties.mail.walletActivatedPath
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
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
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.project.organizationUuid +
                "/" + applicationProperties.mail.manageProjectPath + "/" + testContext.project.uuid
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
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
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.organization.uuid
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
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
            assertThat(mailList).hasSize(1)
        }
    }

    private fun generateUserResponse(email: String): UserResponse =
        UserResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setEmail(email)
            .setEnabled(true)
            .setFirstName("First")
            .setLastName("Last")
            .setCoop(coop)
            .build()

    private fun generateProjectResponse(createdBy: String): ProjectResponse =
        ProjectResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setCreatedByUser(createdBy)
            .setName("Duimane Investement")
            .setOrganizationUuid(UUID.randomUUID().toString())
            .build()

    private fun generateOrganizationResponse(createdBy: String): OrganizationResponse =
        OrganizationResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setCreatedByUser(createdBy)
            .setName("Duimane Investement Organization")
            .build()

    private class TestContext {
        val receiverMail = "test@test.com"
        val tokenIssuerMail = "demoadmin@ampnet.io"
        val token = "test-token"
        val organizationName = "Organization test"
        val amount = 100L
        val receiverEmails = listOf("test@test.com", "test2@test.com")
        lateinit var walletOwner: String
        lateinit var project: ProjectResponse
        lateinit var organization: OrganizationResponse
    }
}
