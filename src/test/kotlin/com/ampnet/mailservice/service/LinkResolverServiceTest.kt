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
            applicationProperties.mail.organizationPath = "/org//invites///"

            val service = LinkResolverServiceImpl(applicationProperties)
            val generatedPath = service.getOrganizationInvitesLink("ampnet-test")
            assertThat(generatedPath).isEqualTo("https://demo.ampnet.io/ampnet-test/org/invites/")
        }
    }

    @Test
    fun mustRemoveDoubleSlashesExceptHttp() {
        verify("Service will remove only correct double slashes") {
            val applicationProperties = ApplicationProperties()
            applicationProperties.mail.baseUrl = "http://demo.ampnet.io/"
            applicationProperties.mail.manageWithdrawalsPath = "//manage/////withdrawals/"

            val service = LinkResolverServiceImpl(applicationProperties)
            val generatedPath = service.getManageWithdrawalsLink("ampnet-test")
            assertThat(generatedPath).isEqualTo("http://demo.ampnet.io/ampnet-test/manage/withdrawals/")
        }
    }
}
