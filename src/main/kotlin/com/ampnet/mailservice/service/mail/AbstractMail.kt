package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.LinkResolver
import com.ampnet.mailservice.service.impl.MailServiceImpl
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.io.StringWriter
import java.util.Date
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

abstract class AbstractMail(
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties
) {
    protected val mustacheFactory = DefaultMustacheFactory()
    protected val linkResolver = LinkResolver(applicationProperties)
    protected abstract val title: String
    protected abstract val template: Mustache
    protected abstract val data: Any

    fun generateMails(to: String): List<MimeMessage> = generateMails(listOf(to))

    fun generateMails(to: List<String>): List<MimeMessage> = createMailMessage(to)

    private fun createMailMessage(to: List<String>): List<MimeMessage> =
        to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail)
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(title)
                helper.setText(fillTemplate(), true)
                helper.setSentDate(Date())
                mail
            } catch (ex: MessagingException) {
                MailServiceImpl.logger.warn { "Cannot create mail from: $to" }
                null
            }
        }

    private fun fillTemplate(): String {
        val writer = StringWriter()
        template.execute(writer, data).flush()
        return writer.toString()
    }
}
