package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class ProjectFullyFundedMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = "projectFullyFundedTemplate"
    override val title = "projectFullyFundedTitle"

    fun setData(user: UserResponse, project: ProjectResponse): ProjectFullyFundedMail {
        data = ProjectFullyFundedData(user, project, linkResolver)
        return this
    }

    fun setTemplate(language: String): ProjectFullyFundedMail {
        template = TemplateRequestData(language, templateName, title)
        return this
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
