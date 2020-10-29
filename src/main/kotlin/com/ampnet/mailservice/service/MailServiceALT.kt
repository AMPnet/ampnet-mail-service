package com.ampnet.mailservice.service

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.impl.LinkResolver
import com.ampnet.mailservice.service.impl.MailServiceImpl
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import java.io.StringWriter
import java.util.Date
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"

@Service
abstract class MailServiceALT<T> (
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    private val userService: UserService? = null,
    private val projectService: ProjectService? = null
) {
    protected val mustacheFactory = DefaultMustacheFactory()
    protected val linkResolver = LinkResolver(applicationProperties)

    abstract fun sendMail(data: T)

    protected fun fillTemplate(template: Mustache, data: Any): String {
        val writer = StringWriter()
        template.execute(writer, data).flush()
        return writer.toString()
    }

    protected fun createMailMessage(to: List<String>, subject: String, text: String): List<MimeMessage> {
        return to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail)
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(subject)
                helper.setText(text, true)
                helper.setSentDate(Date())
                mail
            } catch (ex: MessagingException) {
                MailServiceImpl.logger.warn { "Cannot create mail from: $to" }
                null
            }
        }
    }

    protected fun sendEmails(mails: List<MimeMessage>, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        if (applicationProperties.mail.enabled.not()) {
            MailServiceImpl.logger.warn { "Sending email is disabled. \nEmail: ${mails.first().content}" }
            return
        }
        MailServiceImpl.logger.info { "Sending email: ${mails.first().subject}" }
        val failed = mails.filter { sendEmail(it).not() }
        if (failed.isNotEmpty()) {
            notifySenderOnError.invoke(failed)
        }
    }

    protected fun sendEmail(mail: MimeMessage): Boolean {
        return try {
            mailSender.send(mail)
            MailServiceImpl.logger.info { "Successfully sent email to: ${mail.allRecipients}" }
            true
        } catch (ex: MailException) {
            MailServiceImpl.logger.warn { "Cannot send email to: ${mail.allRecipients}" }
            false
        }
    }

    protected fun sendEmailOnFailedDelivery(mail: MimeMessage) {
        MailServiceImpl.logger.info { "Sending failed delivery email: ${mail.subject}" }
        try {
            mailSender.send(mail)
            MailServiceImpl.logger.info { "Successfully sent failed delivery email to sender: ${mail.sender}" }
        } catch (ex: MailException) {
            MailServiceImpl.logger.warn(ex) { "Cannot send failed delivery email to sender: ${mail.sender}" }
        }
    }
}