package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.impl.FROM_CENTS_TO_EUROS
import com.ampnet.mailservice.service.impl.MailServiceImpl
import com.ampnet.mailservice.service.impl.TWO_DECIMAL_FORMAT
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
    private lateinit var templateService: TemplateService

    @Autowired
    private lateinit var applicationProperties: ApplicationProperties

    private val userService = Mockito.mock(UserService::class.java)

    private lateinit var service: MailServiceImpl
    private lateinit var wiser: Wiser
    private var defaultMailPort: Int = 0
    private val testData = TestData()

    @BeforeEach
    fun init() {
        defaultMailPort = mailSender.port
        wiser = Wiser(0)
        wiser.start()
        mailSender.port = wiser.server.port
        service = MailServiceImpl(mailSender, templateService, applicationProperties, userService)
    }

    @AfterEach
    fun tearDown() {
        wiser.stop()
        mailSender.port = defaultMailPort
    }

    @Test
    fun mustSetCorrectSendConfirmationMail() {
        suppose("Service sent the mail") {
            service.sendConfirmationMail(testData.receiverMail, testData.token)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.confirmationMailSubject)

            val confirmationLink = applicationProperties.mail.baseUrl + "/" +
                "${applicationProperties.mail.confirmationLink}?token=${testData.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            service.sendResetPasswordMail(testData.receiverMail, testData.token)
        }

        verify("The mail is sent to right receiver and has reset password link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.resetPasswordSubject)

            val resetPasswordLink = "${applicationProperties.mail.baseUrl}/" +
                "${applicationProperties.mail.resetPasswordLink}?token=${testData.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(resetPasswordLink)
        }
    }

    @Test
    fun mustSetCorrectOrganizationInvitationMail() {
        suppose("Service send organizationInvitation mail") {
            service.sendOrganizationInvitationMail(testData.receiverMail, testData.organizationName)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.invitationMailSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains(testData.organizationName)

            val link = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsLink
            assertThat(mailText).contains(link)
        }
    }

    @Test
    fun mustSetCorrectDepositRequestMail() {
        suppose("Service send deposit request mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendDepositRequestMail(user, testData.amount)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains((TWO_DECIMAL_FORMAT.format(testData.amount / FROM_CENTS_TO_EUROS)))
        }
    }

    @Test
    fun mustSetCorrectPositiveDepositInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendDepositInfoMail(user, true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("approved")
        }
    }

    @Test
    fun mustSetCorrectNegativeDepositInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendDepositInfoMail(user, false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("rejected")
        }
    }

    @Test
    fun mustSetCorrectWithdrawRequestMail() {
        suppose("User service returns a list of token issuers") {
            val tokenIssuer = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testData.tokenIssuerMail)
                .build()
            Mockito.`when`(userService.getTokenIssuers())
                .thenReturn(listOf(tokenIssuer))
        }
        suppose("Service send withdraw request mail to user and token issuers") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendWithdrawRequestMail(user, testData.amount)
        }

        verify("The mail is sent to token issuer and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(2)
            val tokenIssuerMail = mailList.first()

            assertThat(tokenIssuerMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(tokenIssuerMail.envelopeReceiver).isEqualTo(testData.tokenIssuerMail)
            assertThat(tokenIssuerMail.mimeMessage.subject).isEqualTo(service.manageWithdrawalsSubject)

            val mailText = tokenIssuerMail.mimeMessage.content.toString()
            assertThat(mailText).contains((TWO_DECIMAL_FORMAT.format(testData.amount / FROM_CENTS_TO_EUROS)))
        }
        verify("The mail is sent to user and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList[1]

            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(service.withdrawSubject)
        }
    }

    @Test
    fun mustSetCorrectPositiveWithdrawInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendWithdrawInfoMail(user, true)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("approved")
        }
    }

    @Test
    fun mustSetCorrectNegativeWithdrawInfoMail() {
        suppose("Service send Deposit info mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendWithdrawInfoMail(user, false)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains("rejected")
        }
    }

    @Test
    fun mustSetCorrectSendNewWalletMails() {
        suppose("Service sent mails for new user and project wallet created") {
            val platformManager = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testData.receiverMail)
                .build()
            Mockito.`when`(userService.getPlatformManagers())
                .thenReturn(listOf(platformManager))
            service.sendNewWalletNotificationMail(WalletType.USER)
            service.sendNewWalletNotificationMail(WalletType.PROJECT)
        }

        verify("Both mails are sent to right receivers and have confirmation link") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(service.newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletLink + "/user"
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)

            val projectMail = mailList[1]
            assertThat(projectMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(projectMail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(projectMail.mimeMessage.subject).isEqualTo(service.newWalletSubject)
            val confirmationProjectLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletLink + "/project"
            assertThat(projectMail.mimeMessage.content.toString()).contains(confirmationProjectLink)
        }
    }

    @Test
    fun mustSetCorrectSendNewOrganizationWalletMail() {
        suppose("Service sent mail for new organization wallet created") {
            val platformManager = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testData.receiverMail)
                .build()
            Mockito.`when`(userService.getPlatformManagers())
                .thenReturn(listOf(platformManager))
            service.sendNewWalletNotificationMail(WalletType.ORGANIZATION)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(userMail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(userMail.mimeMessage.subject).isEqualTo(service.newWalletSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.newWalletLink + "/groups"
            assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    private fun generateUserResponse(email: String): UserResponse =
        UserResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setEmail(email)
            .setEnabled(true)
            .setFirstName("First")
            .setLastName("Last")
            .build()

    private class TestData {
        val receiverMail = "test@test.com"
        val tokenIssuerMail = "demoadmin@ampnet.io"
        val token = "test-token"
        val organizationName = "Organization test"
        val amount = 100L
    }
}
