package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.LinkResolverServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties
class LinkResolverServiceTest : TestBase() {

    @Test
    fun mustRemoveDoubleSlashesExceptHttps() {
        verify("Service will remove only correct double slashes") {
            val applicationProperties = ApplicationProperties()
            applicationProperties.mail.baseUrl = "https://demo.ampnet.io/"
            applicationProperties.mail.organizationInvitationsPath = "/org//invites///"

            val service = LinkResolverServiceImpl(applicationProperties)
            val generatedPath = service.getOrganizationInvitesLink()
            assertThat(generatedPath).isEqualTo("https://demo.ampnet.io/org/invites/")
        }
    }

    @Test
    fun mustRemoveDoubleSlashesExceptHttp() {
        verify("Service will remove only correct double slashes") {
            val applicationProperties = ApplicationProperties()
            applicationProperties.mail.baseUrl = "http://demo.ampnet.io/"
            applicationProperties.mail.manageWithdrawalsPath = "//manage/////withdrawals/"

            val service = LinkResolverServiceImpl(applicationProperties)
            val generatedPath = service.getManageWithdrawalsLink()
            assertThat(generatedPath).isEqualTo("http://demo.ampnet.io/manage/withdrawals/")
        }
    }
}
