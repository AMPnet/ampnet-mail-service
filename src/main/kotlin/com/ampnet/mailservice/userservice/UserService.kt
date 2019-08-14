package com.ampnet.mailservice.userservice

import com.ampnet.userservice.proto.UserResponse
import java.util.UUID

interface UserService {
    fun getUsers(uuids: List<UUID>): List<UserResponse>
}
