package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.io.StringReader
import java.io.StringWriter
import java.util.Date
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

abstract class AbstractMail(
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    protected val linkResolver: LinkResolverService,
    private val templateService: TemplateService
) {

    companion object : KLogging()

    abstract val templateName: String
    abstract val title: String
    protected lateinit var template: TemplateRequestData
    protected open var data: Any? = null

    fun sendTo(to: List<String>, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        sendEmails(this.createMailMessage(to), notifySenderOnError)
    }

    fun sendTo(to: String, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        sendEmails(this.createMailMessage(listOf(to)), notifySenderOnError)
    }

    private fun sendEmails(mails: List<MimeMessage>, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        if (applicationProperties.mail.enabled.not()) {
            logger.warn { "Sending email is disabled. \nEmail: ${mails.first().content}" }
            return
        }
        logger.info { "Sending email: ${mails.first().subject}" }
        val failed = mails.filter { sendEmail(it).not() }
        if (failed.isNotEmpty()) {
            notifySenderOnError.invoke(failed)
        }
    }

    private fun sendEmail(mail: MimeMessage): Boolean {
        return try {
            mailSender.send(mail)
            logger.info { "Successfully sent email to: ${mail.allRecipients.joinToString()}" }
            true
        } catch (ex: MailException) {
            logger.warn { "Cannot send email to: ${mail.allRecipients.joinToString()}" }
            false
        }
    }

    private fun createMailMessage(to: List<String>): List<MimeMessage> =
        to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail, "UTF-8")
            val templateTranslation =
                templateService.getTemplateData(template)
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(templateTranslation.title)
                helper.setText(fillTemplate(generateMustache(templateTranslation.template, template.name)), true)
                helper.setSentDate(Date())
                mail
            } catch (ex: MessagingException) {
                logger.warn { "Cannot create mail from: $to" }
                null
            }
        }

    private fun generateMustache(template: String, name: String): Mustache =
        DefaultMustacheFactory().compile(StringReader(template), name)

    private fun fillTemplate(template: Mustache): String {
        val writer = StringWriter()
        template.execute(writer, data).flush()
        return writer.toString()
    }

    data class TemplateRequestData(
        val language: String,
        val name: String,
        val title: String
    )
}

const val EN_LANGUAGE = "en"
const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"
fun Long.toMailFormat(): String = TWO_DECIMAL_FORMAT.format(this / FROM_CENTS_TO_EUROS)
