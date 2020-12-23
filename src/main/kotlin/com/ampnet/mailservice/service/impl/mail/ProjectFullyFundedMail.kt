package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.userservice.proto.UserResponse
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class ProjectFullyFundedMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            EN_LANGUAGE, "Project is fully funded",
            DefaultMustacheFactory().compile("mustache/project-fully-funded-template.mustache")
        )
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
