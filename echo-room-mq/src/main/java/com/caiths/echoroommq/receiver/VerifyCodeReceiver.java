package com.caiths.echoroommq.receiver;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 验证码消息接收器，用于处理验证码队列中的消息并发送邮件。
 * <p>
 * 该类监听验证码消息队列，发送验证码邮件，并通过 Redis 防止消息重复消费。
 * </p>
 *
 * @author poboll
 * @since 2025-02-27
 */
@Component
public class VerifyCodeReceiver {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCodeReceiver.class);

    /**
     * 处理验证码消息队列中的消息。
     * <p>
     * 从消息队列中获取验证码，生成邮件并发送，记录消费状态到 Redis，若失败则重新入队。
     * </p>
     *
     * @param message 接收到的消息对象，包含验证码内容
     * @param channel RabbitMQ 通道，用于手动确认或拒绝消息
     * @throws IOException 如果消息处理或确认过程中发生 IO 异常
     */
    @RabbitListener(queues = "${mail.queue.verifyCode:mail-queue-verifyCode}")
    public void getMessage(Message message, Channel channel) throws IOException {
        // 获取消息内容
        String code = message.getPayload().toString();
        // 获取消息头和唯一标志
        MessageHeaders headers = message.getHeaders();
        Long tag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        String msgId = (String) headers.get("spring_returned_message_correlation");
        LOGGER.info("【" + msgId + "】-正在处理的消息");

        // 检查是否已消费
        if (redisTemplate.opsForHash().entries("mail_log").containsKey(msgId)) {
            channel.basicAck(tag, false);
            LOGGER.info("【" + msgId + "】消息出现重复消费");
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setSubject("回声谷即时通讯系统管理端-验证码验证");
            msg.setText("本次登录的验证码：" + code + "\n请不要泄露您的邮箱验证码");
            msg.setFrom("zysuyy@gmail.com");
            msg.setTo("caiths@icloud.com");
            msg.setSentDate(new Date());
            javaMailSender.send(msg);

            // 记录消息消费状态
            redisTemplate.opsForHash().put("mail_log", msgId, code);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // 处理失败，重新入队
            channel.basicNack(tag, false, true);
            LOGGER.info("【" + msgId + "】消息重新放回到了队列中");
            e.printStackTrace();
        }
    }
}
