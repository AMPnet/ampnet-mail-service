package com.ampnet.mailservice.service.pojo

import com.ampnet.projectservice.proto.ProjectWithDataResponse

data class SuccessfullyInvestedTemplateData(
    val project: ProjectWithDataResponse,
    val amount: Long,
    val coopName: String,
    val attachment: Attachment?
)
