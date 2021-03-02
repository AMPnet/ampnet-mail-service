package com.ampnet.mailservice.amqp

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ErrorHandler

@Configuration
class AmqpConfig(private val objectMapper: ObjectMapper) {

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            messageConverter = jsonConverter()
        }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        configurer: SimpleRabbitListenerContainerFactoryConfigurer
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            configurer.configure(this, connectionFactory)
            this.setErrorHandler(errorHandler())
        }

    @Bean
    fun jsonConverter(): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun errorHandler(): ErrorHandler = ConditionalRejectingErrorHandler(ExceptionStrategy())
}
