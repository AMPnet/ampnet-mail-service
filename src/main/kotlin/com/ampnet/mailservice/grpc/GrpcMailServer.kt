package com.ampnet.mailservice.grpc

import com.ampnet.mailservice.proto.DepositInfoRequest
import com.ampnet.mailservice.proto.DepositRequest
import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.WithdrawInfoRequest
import com.ampnet.mailservice.proto.WithdrawRequest
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.userservice.UserService
import com.ampnet.userservice.proto.UserResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID

@GrpcService
class GrpcMailServer(
    private val mailService: MailService,
    private val userService: UserService
) : MailServiceGrpc.MailServiceImplBase() {

    companion object : KLogging()

    override fun sendMailConfirmation(request: MailConfirmationRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendMailConfirmationRequest to: ${request.email}" }
        mailService.sendConfirmationMail(request.email, request.token)
        returnEmptyResponse(responseObserver)
    }

    override fun sendOrganizationInvitation(
        request: OrganizationInvitationRequest,
        responseObserver: StreamObserver<Empty>
    ) {
        logger.debug { "Received gRPC request SendOrganizationInvitationRequest to: ${request.email}" }
        mailService.sendOrganizationInvitationMail(request.email, request.organization)
        returnEmptyResponse(responseObserver)
    }

    override fun sendDepositRequest(request: DepositRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendDepositRequest to: ${request.user}" }
        sendMailToUser(request.user, responseObserver) {
            mailService.sendDepositRequestMail(it, request.amount)
        }
    }

    override fun sendDepositInfo(request: DepositInfoRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendDepositInfo to: ${request.user}" }

        sendMailToUser(request.user, responseObserver) {
            mailService.sendDepositInfoMail(it, request.minted)
        }
    }

    override fun sendWithdrawRequest(request: WithdrawRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendWithdrawRequest to: ${request.user}" }

        sendMailToUser(request.user, responseObserver) {
            mailService.sendWithdrawRequestMail(it, request.amount)
        }
    }

    override fun sendWithdrawInfo(request: WithdrawInfoRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendWithdrawInfo to: ${request.user}" }
        sendMailToUser(request.user, responseObserver) {
            mailService.sendWithdrawInfoMail(it, request.burned)
        }
    }

    private fun sendMailToUser(
        uuid: String,
        observer: StreamObserver<Empty>,
        sendMail: (user: UserResponse) -> (Unit)
    ) {
        val userResponse = getUserResponse(uuid)
        if (userResponse != null) {
            sendMail(userResponse)
            returnEmptyResponse(observer)
        } else {
            returnErrorForMissingUser(observer, uuid)
        }
    }

    private fun returnEmptyResponse(responseObserver: StreamObserver<Empty>) {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }

    private fun returnErrorForMissingUser(responseObserver: StreamObserver<Empty>, user: String) {
        responseObserver.onError(
            Status.INVALID_ARGUMENT.withDescription("Missing user with uuid: $user")
                .asRuntimeException())
    }

    private fun getUserResponse(user: String): UserResponse? {
        return try {
            val uuid = UUID.fromString(user)
            val users = userService.getUsers(listOf(uuid))
            users.firstOrNull()
        } catch (ex: IllegalArgumentException) {
            logger.warn(ex) { "User uuid in invalid form" }
            null
        }
    }
}
