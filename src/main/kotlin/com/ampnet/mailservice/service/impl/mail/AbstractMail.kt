package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.pojo.Attachment
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import mu.KLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.io.StringReader
import java.io.StringWriter
import java.util.Date
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

abstract class AbstractMail(
    protected val linkResolver: LinkResolverService,
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    private val cmsService: CmsService
) {

    companion object : KLogging()

    protected abstract val mailType: MailType
    protected open var language: Lang = Lang.EN
    protected lateinit var coop: String
    protected open var attachment: Attachment? = null
    protected open var templateData: Any? = null

    fun setLanguage(language: String) = apply {
        this.language = Lang.langOrDefault(language)
    }

    fun setCoop(coop: String) = apply { this.coop = coop }

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
            logger.warn(ex) { "Cannot send email to: ${mail.allRecipients.joinToString()}" }
            false
        }
    }

    private fun createMailMessage(to: List<String>): List<MimeMessage> {
        val mailTranslation = cmsService.getMail(coop, mailType, language)
        val template = DefaultMustacheFactory().compile(StringReader(mailTranslation.content), mailType.name)
        return to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            try {
                val helper = if (attachment != null) {
                    MimeMessageHelper(mail, true, UTF_8_ENCODING)
                } else {
                    MimeMessageHelper(mail, UTF_8_ENCODING)
                }
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(mailTranslation.title)
                helper.setText(fillTemplate(template), true)
                helper.setSentDate(Date())
                attachment?.let { attachment ->
                    helper.addAttachment(attachment.name, ByteArrayResource(attachment.file))
                }
                mail
            } catch (ex: MessagingException) {
                logger.warn(ex) { "Cannot create mail from: $to" }
                null
            }
        }
    }

    private fun fillTemplate(template: Mustache): String {
        val writer = StringWriter()
        template.execute(writer, templateData).flush()
        return writer.toString()
    }
}

const val UTF_8_ENCODING = "UTF-8"
const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"
fun Long.toMailFormat(): String = TWO_DECIMAL_FORMAT.format(this / FROM_CENTS_TO_EUROS)
