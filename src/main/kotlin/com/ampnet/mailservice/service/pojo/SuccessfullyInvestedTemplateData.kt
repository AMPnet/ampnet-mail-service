package com.ampnet.mailservice.service.pojo

import com.ampnet.projectservice.proto.ProjectWithDataResponse
import com.ampnet.userservice.proto.CoopResponse

data class SuccessfullyInvestedTemplateData(
    val project: ProjectWithDataResponse,
    val amount: Long,
    val coopResponse: CoopResponse,
    val attachment: Attachment?
)
