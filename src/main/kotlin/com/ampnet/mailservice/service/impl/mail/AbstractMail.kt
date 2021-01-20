package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
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
    protected val linkResolver: LinkResolverService,
    private val translationService: TranslationService
) {

    companion object : KLogging()

    abstract val templateName: String
    abstract val titleKey: String
    protected lateinit var language: String
    protected open var data: Any? = null
    private val templateTranslations: Map<String, Mustache> by lazy {
        translationService.getTemplateTranslations(templateName)
    }
    private val titleTranslations: Map<String, String> by lazy { translationService.getTitleTranslations(titleKey) }

    fun setLanguage(language: String) = apply { this.language = language }

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
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(getTitle())
                helper.setText(fillTemplate(getTemplate()), true)
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

    private fun getTitle(): String {
        return titleTranslations[language] ?: titleTranslations[EN_LANGUAGE]
            ?: throw InternalException("Could not find default[en] title")
    }

    private fun getTemplate(): Mustache {
        return templateTranslations[language] ?: templateTranslations[EN_LANGUAGE]
            ?: throw InternalException("Could not find default[en] template")
    }
}

const val EN_LANGUAGE = "en"
const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"
fun Long.toMailFormat(): String = TWO_DECIMAL_FORMAT.format(this / FROM_CENTS_TO_EUROS)
