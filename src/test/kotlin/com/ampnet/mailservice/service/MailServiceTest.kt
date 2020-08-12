package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.impl.MailServiceImpl
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

            val confirmationLink = "${applicationProperties.mail.confirmationBaseLink}?token=${testData.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
        }
    }

    @Test
    fun mustSetCorrectSendResetPasswordMail() {
        suppose("Service sent reset password mail") {
            service.sendResetPasswordMail(testData.receiverMail, testData.token)
        }

        verify("The mail is sent to right receiver and has confirmation link") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.resetPasswordSubject)

            val confirmationLink = "${applicationProperties.mail.resetPasswordBaseLink}?token=${testData.token}"
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
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
            assertThat(mailText).contains(applicationProperties.mail.organizationInvitationsLink)
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
            assertThat(mailText).contains(testData.amount.toString())
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
        suppose("Service send withdraw request mail") {
            val user = generateUserResponse(testData.receiverMail)
            service.sendWithdrawRequestMail(user, testData.amount)
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.withdrawSubject)

            val mailText = mail.mimeMessage.content.toString()
            assertThat(mailText).contains(testData.amount.toString())
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
    fun mustSetCorrectSendNewWalletMail() {
        suppose("Service sent New wallet created mail") {
            val platformManager = UserResponse.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setEmail(testData.receiverMail)
                .build()
            Mockito.`when`(userService.getPlatformManagers())
                .thenReturn(listOf(platformManager))
            service.sendNewWalletNotificationMail()
        }

        verify("The mail is sent to right receivers and has confirmation link") {
            val mailList = wiser.messages
            val mail = mailList.first()
            assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            assertThat(mail.envelopeReceiver).isEqualTo(testData.receiverMail)
            assertThat(mail.mimeMessage.subject).isEqualTo(service.newWalletSubject)

            val confirmationLink = applicationProperties.mail.newWalletLink
            assertThat(mail.mimeMessage.content.toString()).contains(confirmationLink)
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
        val token = "test-token"
        val organizationName = "Organization test"
        val amount = 100L
    }
}
