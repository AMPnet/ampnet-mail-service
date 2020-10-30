package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.pojo.OrganizationInvitationRequestData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class OrganizationInvitationServiceTest : ServiceTestBase() {

    private val testContext = TestContext()
    private val service: OrganizationInvitationService by lazy {
        OrganizationInvitationService(mailSender, applicationProperties)
    }

    @Test
    fun mustSetCorrectOrganizationInvitationMail() {
        suppose("Service sends organizationInvitation e-mails") {
            service.sendMail(
                OrganizationInvitationRequestData(
                    testContext.receiverEmails, testContext.organizationName, "sender@email.com"
                )
            )
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(2)
            val firstMail = mailList.first()
            val secondMail = mailList.last()
            Assertions.assertThat(firstMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(firstMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.first())
            Assertions.assertThat(firstMail.mimeMessage.subject).isEqualTo(service.invitationMailSubject)
            Assertions.assertThat(secondMail.envelopeReceiver).isEqualTo(testContext.receiverEmails.last())

            val mailText = firstMail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains(testContext.organizationName)

            val link = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath
            Assertions.assertThat(mailText).contains(link)
        }
    }

    private class TestContext {
        val organizationName = "Organization test"
        val receiverEmails = listOf("test@test.com", "test2@test.com")
    }
}
