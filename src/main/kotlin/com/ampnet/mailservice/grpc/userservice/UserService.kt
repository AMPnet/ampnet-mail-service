package com.ampnet.mailservice.grpc.userservice

import com.ampnet.userservice.proto.UserResponse

interface UserService {
    fun getUsers(uuids: List<String>): List<UserResponse>
    fun getPlatformManagers(coop: String): List<UserResponse>
    fun getTokenIssuers(coop: String): List<UserResponse>
}
