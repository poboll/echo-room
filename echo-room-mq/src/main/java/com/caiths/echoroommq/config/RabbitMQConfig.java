package com.caiths.echoroommq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类，用于定义邮件相关的交换机、队列和绑定关系。
 * <p>
 * 该类通过 Spring 的配置方式，创建并配置邮件服务的 DirectExchange、验证码队列和反馈队列，并绑定路由键。
 * </p>
 *
 * @author poboll
 * @since 2025-02-27
 */
@Configuration
public class RabbitMQConfig {

    @Value("${mail.exchange:mail-exchange}")
    private String mailExchange;

    @Value("${mail.queue.verifyCode:mail-queue-verifyCode}")
    private String mailQueueVerifyCode;

    @Value("${mail.route.verifyCode:mail-route-verifyCode}")
    private String mailRouteVerifyCode;

    @Value("${mail.queue.feedback:mail-queue-feedback}")
    private String mailQueueFeedback;

    @Value("${mail.route.feedback:mail-route-feedback}")
    private String mailRouteFeedback;

    /**
     * 创建邮件服务使用的 DirectExchange。
     * <p>
     * 该交换机用于路由邮件相关消息，配置为持久化且非自动删除。
     * </p>
     *
     * @return 配置好的 DirectExchange 实例
     */
    @Bean
    DirectExchange mailExchange() {
        return new DirectExchange(mailExchange, true, false);
    }

    /**
     * 创建验证码消息队列。
     * <p>
     * 该队列用于接收验证码相关的消息，配置为持久化队列。
     * </p>
     *
     * @return 配置好的 Queue 实例
     */
    @Bean
    Queue mailQueueVerifyCode() {
        return new Queue(mailQueueVerifyCode, true);
    }

    /**
     * 绑定验证码队列到邮件交换机。
     * <p>
     * 使用指定的路由键将验证码队列绑定到邮件交换机，确保消息正确路由。
     * </p>
     *
     * @return Binding 实例，表示绑定关系
     */
    @Bean
    Binding mailQueueVerifyCodeBinding() {
        return BindingBuilder.bind(mailQueueVerifyCode()).to(mailExchange()).with(mailRouteVerifyCode);
    }

    /**
     * 创建反馈消息队列。
     * <p>
     * 该队列用于接收用户反馈相关的消息，配置为持久化队列。
     * </p>
     *
     * @return 配置好的 Queue 实例
     */
    @Bean
    Queue mailQueueFeedback() {
        return new Queue(mailQueueFeedback, true);
    }

    /**
     * 绑定反馈队列到邮件交换机。
     * <p>
     * 使用指定的路由键将反馈队列绑定到邮件交换机，确保消息正确路由。
     * </p>
     *
     * @return Binding 实例，表示绑定关系
     */
    @Bean
    Binding mailQueueFeedbackBinding() {
        return BindingBuilder.bind(mailQueueFeedback()).to(mailExchange()).with(mailRouteFeedback);
    }
}
