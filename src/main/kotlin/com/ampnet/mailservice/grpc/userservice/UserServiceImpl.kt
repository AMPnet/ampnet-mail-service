package com.ampnet.mailservice.grpc.userservice

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.GrpcException
import com.ampnet.userservice.proto.CoopRequest
import com.ampnet.userservice.proto.GetUsersRequest
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserServiceGrpc
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

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

    @Throws(GrpcException::class)
    override fun getPlatformManagers(coop: String): List<UserResponse> {
        logger.debug { "Fetching Platform Managers for coop: $coop" }
        try {
            val usersResponse = serviceBlockingStub
                .withDeadlineAfter(applicationProperties.grpc.userServiceTimeout, TimeUnit.MILLISECONDS)
                .getPlatformManagers(getCoopRequest(coop))
                .usersList
            logger.debug { "Users response: $usersResponse" }
            return usersResponse
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Could not get Platform Managers from user service", ex)
        }
    }

    override fun getTokenIssuers(coop: String): List<UserResponse> {
        logger.debug { "Fetching Token Issuers for coop: $coop" }
        try {
            val usersResponse = serviceBlockingStub
                .withDeadlineAfter(applicationProperties.grpc.userServiceTimeout, TimeUnit.MILLISECONDS)
                .getTokenIssuers(getCoopRequest(coop))
                .usersList
            logger.debug { "Users response: $usersResponse" }
            return usersResponse
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Could not get Platform Managers from user service", ex)
        }
    }

    fun getCoopRequest(coop: String): CoopRequest {
        return CoopRequest.newBuilder()
            .setCoop(coop)
            .build()
    }
}
