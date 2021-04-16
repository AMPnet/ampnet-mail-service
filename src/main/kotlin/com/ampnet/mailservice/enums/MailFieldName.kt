package com.ampnet.mailservice.enums

enum class MailFieldName(val value: String) {

    ORGANIZATION("{{organization}}"),
    LINK("{{& link}}"),
    ACTIVATION_DATA("{{activationData}}"),
    PROJECT_NAME("{{projectName}}"),
    AMOUNT("{{amount}}"),
    FAILED_RECIPIENTS("{{failedRecipients}}"),
    FIRST_NAME("{{firstName}}"),
    PROJECT_DESCRIPTION("{{projectDescription}}"),
    COOP("{{coop}}"),
    LAST_NAME("{{lastName}}");
}
