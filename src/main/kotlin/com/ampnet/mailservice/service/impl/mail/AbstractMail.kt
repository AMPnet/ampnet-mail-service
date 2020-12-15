package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.Mustache
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.io.StringWriter
import java.util.Date
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

abstract class AbstractMail(
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    protected val linkResolver: LinkResolverService
) {

    companion object : KLogging()

    protected abstract val languageData: List<LanguageData>
    protected open var data: Any? = null

    fun sendTo(
        to: List<String>,
        language: String = EN_LANGUAGE,
        notifySenderOnError: (List<MimeMessage>) -> Unit = {}
    ) {
        sendEmails(this.createMailMessage(to, language), notifySenderOnError)
    }

    fun sendTo(to: String, language: String = EN_LANGUAGE, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        sendEmails(this.createMailMessage(listOf(to), language), notifySenderOnError)
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

    private fun createMailMessage(to: List<String>, language: String): List<MimeMessage> =
        to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail)
            val languageData = getLanguageData(language)
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(languageData.title)
                helper.setText(fillTemplate(languageData.template), true)
                helper.setSentDate(Date())
                mail
            } catch (ex: MessagingException) {
                logger.warn { "Cannot create mail from: $to" }
                null
            }
        }

    private fun fillTemplate(template: Mustache): String {
        val writer = StringWriter()
        template.execute(writer, data).flush()
        return writer.toString()
    }

    private fun getLanguageData(language: String): LanguageData =
        languageData.firstOrNull { it.language == language } ?: languageData.first { it.language == EN_LANGUAGE }

    data class LanguageData(
        val language: String,
        val title: String,
        val template: Mustache
    )
}

const val EN_LANGUAGE = "en"
const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"
fun Long.toMailFormat(): String = TWO_DECIMAL_FORMAT.format(this / FROM_CENTS_TO_EUROS)
