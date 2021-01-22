package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class ProjectFullyFundedMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "projectFullyFundedTemplate"
    override val titleKey = "projectFullyFundedTitle"

    fun setTemplateData(user: UserResponse, project: ProjectResponse) = apply {
        templateData = ProjectFullyFundedData(user, project, linkResolver)
    }
}
data class ProjectFullyFundedData(
    val firstName: String,
    val projectName: String,
    val link: String
) {
    constructor(user: UserResponse, project: ProjectResponse, linkResolver: LinkResolverService) : this(
        user.firstName,
        project.name,
        linkResolver.getProjectFullyFundedLink(user.coop, project.organizationUuid, project.uuid)
    )
}
