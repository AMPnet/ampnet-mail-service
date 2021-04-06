package com.ampnet.mailservice.enums

@Suppress("MagicNumber")
enum class MailType(val id: Int, val defaultTemplateKey: String, val defaultTitleKey: String) {

    ACTIVATED_USER_WALLET_MAIL(
        1,
        "userWalletActivatedTemplate",
        "walletActivatedTitle"
    ) {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.ACTIVATION_DATA, MailFieldName.LINK)
    },
    ACTIVATED_ORGANIZATION_WALLET_MAIL(
        2,
        "organizationWalletActivatedTemplate",
        "walletActivatedTitle"
    ) {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.LINK)
    },
    ACTIVATED_PROJECT_WALLET_MAIL(
        3,
        "projectWalletActivatedTemplate",
        "walletActivatedTitle"
    ) {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.PROJECT_NAME, MailFieldName.LINK)
    },
    MAIL_CONFIRMATION_MAIL(4, "mailConfirmationTemplate", "confirmationTitle") {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.LINK)
    },
    DEPOSIT_INFO_MAIL(5, "depositTemplate", "depositInfoTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.COOP, MailFieldName.LINK)
    },
    DEPOSIT_FAILED_INFO_MAIL(6, "depositFailedTemplate", "depositInfoTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf()
    },
    DEPOSIT_INFO_NO_PROJECT_TO_INVEST_MAIL(7, "depositNoProjectToInvestTemplate", "depositInfoTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.COOP)
    },
    DEPOSIT_REQUEST_MAIL(8, "depositRequestTemplate", "depositInfoTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.AMOUNT)
    },
    FAILED_DELIVERY_MAIL(9, "failedDeliveryMessageTemplate", "failedDeliveryTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.FAILED_RECIPIENTS)
    },
    INVITATION_MAIL(10, "invitationTemplate", "invitationTitle") {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.ORGANIZATION, MailFieldName.LINK)
    },
    NEW_USER_WALLET_MAIL(11, "userWalletTemplate", "newWalletTitle") {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.ACTIVATION_DATA, MailFieldName.ORGANIZATION, MailFieldName.LINK)
    },
    NEW_PROJECT_WALLET_MAIL(12, "projectWalletTemplate", "newWalletTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.LINK)
    },
    NEW_ORGANIZATION_WALLET_MAIL(13, "organizationWalletTemplate", "newWalletTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.LINK)
    },
    PROJECT_FULLY_FUNDED_MAIL(14, "projectFullyFundedTemplate", "projectFullyFundedTitle") {
        override fun getRequiredFields(): List<MailFieldName> =
            listOf(MailFieldName.FIRST_NAME, MailFieldName.PROJECT_NAME, MailFieldName.LINK)
    },
    RESET_PASSWORD_MAIL(15, "forgotPasswordTemplate", "resetPasswordTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.LINK)
    },
    SUCCESSFULLY_INVESTED_MAIL(16, "investmentTemplate", "investmentTitle") {
        override fun getRequiredFields(): List<MailFieldName> {
            return listOf(
                MailFieldName.AMOUNT, MailFieldName.PROJECT_NAME, MailFieldName.PROJECT_DESCRIPTION, MailFieldName.COOP
            )
        }
    },
    SUCCESSFULLY_INVESTED_WITHOUT_TOS_MAIL(17, "investmentWithoutTosTemplate", "investmentTitle") {
        override fun getRequiredFields(): List<MailFieldName> {
            return listOf(
                MailFieldName.AMOUNT, MailFieldName.PROJECT_NAME, MailFieldName.PROJECT_DESCRIPTION, MailFieldName.COOP
            )
        }
    },
    WITHDRAW_INFO_MAIL(18, "withdrawTemplate", "withdrawTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf()
    },
    WITHDRAW_FAILED_INFO_MAIL(19, "withdrawFailedTemplate", "withdrawTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf()
    },
    WITHDRAW_REQUEST_MAIL(20, "withdrawRequestTemplate", "withdrawTitle") {
        override fun getRequiredFields(): List<MailFieldName> = listOf(MailFieldName.AMOUNT)
    },
    TOKEN_ISSUER_WITHDRAWAL_REQUEST_MAIL(21, "tokenIssuerWithdrawalTemplate", "newWithdrawTitle") {
        override fun getRequiredFields(): List<MailFieldName> {
            return listOf(MailFieldName.FIRST_NAME, MailFieldName.LAST_NAME, MailFieldName.AMOUNT, MailFieldName.LINK)
        }
    };

    companion object {
        private val map = values().associateBy(MailType::id)
        fun fromInt(type: Int) = map[type]
    }

    abstract fun getRequiredFields(): List<MailFieldName>
}
