package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender

class ProjectFullyFundedMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "project-fully-funded-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, "Project is fully funded"),
        generateLanguageData(EL_LANGUAGE, templateName, "Το έργο χρηματοδοτείται πλήρως")
    )

    fun setData(user: UserResponse, project: ProjectResponse): ProjectFullyFundedMail {
        data = ProjectFullyFundedData(user, project, linkResolver)
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
