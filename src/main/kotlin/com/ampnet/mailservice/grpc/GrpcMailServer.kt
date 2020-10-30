package com.ampnet.mailservice.grpc

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.exception.GrpcException
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.proto.ActivatedWalletRequest
import com.ampnet.mailservice.proto.DepositInfoRequest
import com.ampnet.mailservice.proto.DepositRequest
import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.mailservice.proto.WalletTypeRequest
import com.ampnet.mailservice.proto.WithdrawInfoRequest
import com.ampnet.mailservice.proto.WithdrawRequest
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.MailServiceALT
import com.ampnet.mailservice.service.pojo.DepositRequestData
import com.ampnet.mailservice.service.pojo.OrganizationInvitationRequestData
import com.ampnet.mailservice.service.pojo.WalletActivatedRequestData
import com.ampnet.userservice.proto.UserResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Autowired
import com.ampnet.mailservice.proto.WalletType as WalletTypeProto

@GrpcService
@Suppress("TooManyFunctions")
class GrpcMailServer(
    private val mailService: MailService,
    private val userService: UserService
) : MailServiceGrpc.MailServiceImplBase() {

    @Autowired
    private lateinit var depositService: MailServiceALT<DepositRequestData>
    @Autowired
    private lateinit var organizationInvitationService: MailServiceALT<OrganizationInvitationRequestData>
    @Autowired
    private lateinit var walletActivatedService: MailServiceALT<WalletActivatedRequestData>

    companion object : KLogging()

    override fun sendMailConfirmation(request: MailConfirmationRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendMailConfirmationRequest to: ${request.email}" }
        mailService.sendConfirmationMail(request.email, request.token)
        returnSuccessfulResponse(responseObserver)
    }

    override fun sendOrganizationInvitation(
        request: OrganizationInvitationRequest,
        responseObserver: StreamObserver<Empty>
    ) {
        val emails = request.emailsList.toList()
        logger.debug { "Received gRPC request sendOrganizationInvitation to: ${emails.joinToString()}" }
        organizationInvitationService.sendMail(
            OrganizationInvitationRequestData(emails, request.organization, request.senderEmail)
        )
        returnSuccessfulResponse(responseObserver)
    }

    override fun sendDepositRequest(request: DepositRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendDepositRequest to: ${request.user}" }
        sendMailToUser(request.user, responseObserver) {
            depositService.sendMail(DepositRequestData(it, request.amount))
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

    override fun sendResetPassword(request: ResetPasswordRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendForgotPassword to: ${request.email}" }
        mailService.sendResetPasswordMail(request.email, request.token)
    }

    override fun sendNewWalletMail(request: WalletTypeRequest, responseObserver: StreamObserver<Empty>?) {
        logger.debug { "Received gRPC request SendNewWalletMail for wallet type: ${request.type}" }
        mailService.sendNewWalletNotificationMail(getWalletType(request.type), request.coop)
    }

    override fun sendWalletActivated(request: ActivatedWalletRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendNewWalletActivated for wallet type: ${request.type}" }
        walletActivatedService.sendMail(WalletActivatedRequestData(request.walletOwner, getWalletType(request.type)))
    }

    private fun sendMailToUser(
        uuid: String,
        observer: StreamObserver<Empty>,
        sendMail: (user: UserResponse) -> (Unit)
    ) {
        try {
            val users = userService.getUsers(listOf(uuid))
            if (users.isEmpty()) {
                returnErrorForMissingUser(observer, uuid)
            } else {
                sendMail(users.first())
                returnSuccessfulResponse(observer)
            }
        } catch (ex: GrpcException) {
            logger.error(ex) { "Cannot get user: $uuid from user service" }
            observer.onError(
                Status.UNAVAILABLE
                    .withDescription("User service is unavailable")
                    .asRuntimeException()
            )
        }
    }

    private fun returnSuccessfulResponse(responseObserver: StreamObserver<Empty>) {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }

    private fun returnErrorForMissingUser(responseObserver: StreamObserver<Empty>, user: String) {
        logger.warn { "Missing user: $user" }
        responseObserver.onError(
            Status.INVALID_ARGUMENT
                .withDescription("Missing user with uuid: $user")
                .asRuntimeException()
        )
    }

    private fun getWalletType(type: WalletTypeProto): WalletType =
        when (type) {
            WalletTypeProto.USER -> WalletType.USER
            WalletTypeProto.PROJECT -> WalletType.PROJECT
            WalletTypeProto.ORGANIZATION -> WalletType.ORGANIZATION
            else -> throw IllegalArgumentException("Invalid wallet type")
        }
}
