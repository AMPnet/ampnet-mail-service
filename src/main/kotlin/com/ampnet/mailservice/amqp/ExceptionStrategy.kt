package com.ampnet.mailservice.amqp

import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler.DefaultExceptionStrategy

class ExceptionStrategy : DefaultExceptionStrategy() {
    override fun isFatal(t: Throwable): Boolean = super.isFatal(t) ||
        t.cause is InternalException || t.cause is ResourceNotFoundException
}
