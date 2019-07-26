package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.MailServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.subethamail.wiser.Wiser

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
        service = MailServiceImpl(mailSender, templateService, applicationProperties)
    }

    @AfterEach
    fun tearDown() {
        wiser.stop()
        mailSender.port = defaultMailPort
    }

    @Test
    fun mustSetCorrectSenderMailFromProperties() {
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

    private class TestData {
        val receiverMail = "test@test.com"
        val token = "test-token"
        val organizationName = "Organization test"
    }
}
