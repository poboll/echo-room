package com.caiths.echoroommq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EchoRoom 消息队列应用的启动类。
 * <p>
 * 该类是 Spring Boot 应用的入口，负责启动整个消息队列服务。
 * </p>
 *
 * @author poboll
 * @since 2025-02-27
 */
@SpringBootApplication
public class EchoRoomMqApplication {

    /**
     * 应用程序的主入口方法。
     * <p>
     * 通过 SpringApplication 启动 EchoRoomMqApplication，加载 Spring Boot 配置。
     * </p>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EchoRoomMqApplication.class, args);
    }
}
