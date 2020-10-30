package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.pojo.DepositRequestData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class DepositServiceTest : ServiceTestBase() {

    private val testContext = TestContext()
    private val service: DepositService by lazy {
        DepositService(mailSender, applicationProperties)
    }

    @Test
    fun mustSetCorrectDepositRequestMail() {
        suppose("Service send deposit request mail") {
            val user = generateUserResponse(testContext.receiverMail)
            service.sendMail(DepositRequestData(user, testContext.amount))
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            Assertions.assertThat(mailList).hasSize(1)
            val mail = mailList.first()
            Assertions.assertThat(mail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(mail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(mail.mimeMessage.subject).isEqualTo(service.depositSubject)

            val mailText = mail.mimeMessage.content.toString()
            Assertions.assertThat(mailText).contains((com.ampnet.mailservice.service.impl.TWO_DECIMAL_FORMAT.format(testContext.amount / com.ampnet.mailservice.service.impl.FROM_CENTS_TO_EUROS)))
        }
    }

    private class TestContext {
        val receiverMail = "test@test.com"
        val amount = 100L
    }
}
