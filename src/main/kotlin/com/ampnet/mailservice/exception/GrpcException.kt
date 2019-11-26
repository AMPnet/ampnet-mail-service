package com.ampnet.mailservice.exception

class GrpcException(exceptionMessage: String, throwable: Throwable) : Exception(exceptionMessage, throwable)
