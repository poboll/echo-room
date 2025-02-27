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
import com.caiths.echoroomapi.entity.Feedback;
import com.caiths.echoroomapi.utils.JsonUtil;

import java.io.IOException;
import java.util.Date;

/**
 * 用户反馈消息接收器，用于处理反馈队列中的消息并发送邮件通知。
 * <p>
 * 该类监听反馈消息队列，解析消息内容为 Feedback 对象，生成邮件并发送，同时通过 Redis 防止消息重复消费。
 * </p>
 *
 * @author poboll
 * @since 2025-02-27
 */
@Component
public class FeedbackReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackReceiver.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 处理反馈消息队列中的消息。
     * <p>
     * 从消息队列中获取用户反馈信息，生成邮件并发送，记录消费状态到 Redis，若失败则重新入队。
     * </p>
     *
     * @param message 接收到的消息对象，包含反馈内容
     * @param channel RabbitMQ 通道，用于手动确认或拒绝消息
     * @throws IOException 如果消息处理或确认过程中发生 IO 异常
     */
    @RabbitListener(queues = "${mail.queue.feedback:mail-queue-feedback}")
    public void getFeedbackMessage(Message message, Channel channel) throws IOException {
        // 获取消息内容
        String s = message.getPayload().toString();
        // 获取消息的唯一标志
        MessageHeaders headers = message.getHeaders();
        Long tag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        String msgId = headers.get("spring_returned_message_correlation").toString();
        LOGGER.info("【" + msgId + "】-正在处理的消息");

        // 检查是否已消费
        if (redisTemplate.opsForHash().entries("mail_log").containsKey(msgId)) {
            channel.basicAck(tag, true);
            LOGGER.info("【" + msgId + "】消息出现重复消费");
            return;
        }

        try {
            Feedback feedback = JsonUtil.parseToObject(s, Feedback.class);
            System.out.println(feedback.getContent());

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setSubject("来自用户的意见反馈");

            // 格式化反馈信息
            StringBuilder formatMessage = new StringBuilder();
            formatMessage.append("用户编号：" + feedback.getUserId() + "\n");
            formatMessage.append("用户名：" + feedback.getUsername() + "\n");
            formatMessage.append("用户昵称：" + feedback.getNickname() + "\n");
            formatMessage.append("反馈内容：" + feedback.getContent());
            System.out.println(">>>>>>>>>>>>>>" + formatMessage + "<<<<<<<<<<<<<<<<<<");

            // 设置邮件内容
            mailMessage.setText(formatMessage.toString());
            mailMessage.setFrom("zysuyy@gmail.com");
            mailMessage.setTo("caiths@icloud.com");
            mailMessage.setSentDate(new Date());
            javaMailSender.send(mailMessage);

            // 记录消息消费状态
            redisTemplate.opsForHash().put("mail_log", msgId, feedback.getContent());
            channel.basicAck(tag, true);
        } catch (Exception e) {
            // 处理失败，重新入队
            channel.basicNack(tag, false, true);
            LOGGER.info("【" + msgId + "】消息重新放回到了队列中");
            e.printStackTrace();
        }
    }
}
