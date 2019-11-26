package com.ampnet.mailservice.grpc.userservice

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.GrpcException
import com.ampnet.userservice.proto.GetUsersRequest
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserServiceGrpc
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : UserService {

    companion object : KLogging()

    private val serviceBlockingStub: UserServiceGrpc.UserServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("user-service")
        UserServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getUsers(uuids: List<String>): List<UserResponse> {
        val userSet = uuids.toSet()
        logger.debug { "Fetching users: $userSet" }
        try {
            val request = GetUsersRequest.newBuilder()
                .addAllUuids(userSet)
                .build()
            val usersResponse = serviceBlockingStub
                .withDeadlineAfter(applicationProperties.grpc.userServiceTimeout, TimeUnit.MILLISECONDS)
                .getUsers(request).usersList
            logger.debug { "Users response: $usersResponse" }
            return usersResponse
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Could not get users from user service", ex)
        }
    }
}
