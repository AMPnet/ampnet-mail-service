package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.service.TemplateService
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.stereotype.Service
import java.io.StringWriter

@Service
class TemplateServiceImpl : TemplateService {

    private val mustacheFactory = DefaultMustacheFactory()
    private val mailConfirmationTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/mail-confirmation-template.mustache")
    }
    private val invitationTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/invitation-template.mustache")
    }

    override fun generateTextForMailConfirmation(data: MailConfirmationData): String {
        return fillTemplate(mailConfirmationTemplate, data)
    }

    override fun generateTextForInvitation(data: InvitationData): String {
        return fillTemplate(invitationTemplate, data)
    }

    private fun fillTemplate(template: Mustache, data: Any): String {
        val writer = StringWriter()
        template.execute(writer, data).flush()
        return writer.toString()
    }
}
