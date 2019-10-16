package com.ampnet.mailservice.userservice

import com.ampnet.userservice.proto.GetUsersRequest
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserServiceGrpc
import java.util.UUID
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory
) : UserService {

    companion object : KLogging()

    private val serviceBlockingStub: UserServiceGrpc.UserServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("user-service")
        UserServiceGrpc.newBlockingStub(channel)
    }

    override fun getUsers(uuids: List<UUID>): List<UserResponse> {
        val set = uuids.toSet()
        logger.debug { "Fetching users: $set" }
        val request = GetUsersRequest.newBuilder()
                .addAllUuids(set.map { it.toString() })
                .build()
        val users = serviceBlockingStub.getUsers(request).usersList
        logger.debug { "Users response: $users" }
        return users
    }
}
