package com.example.campussystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统启动监听器
 * 在应用启动完成后显示访问信息
 */
@Component
public class StartupListener {

    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Value("${spring.application.name:campus-trading-system}")
    private String applicationName;

    private final Environment environment;

    public StartupListener(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String localAddress = "localhost";
            
            // 构建访问URL
            String localUrl = String.format("http://%s:%s%s", localAddress, port, contextPath);
            String networkUrl = String.format("http://%s:%s%s", hostAddress, port, contextPath);
            
            // 简化的启动成功信息
            logger.info("\n" +
                "🎉 {} 启动成功！\n" +
                "📍 本地访问: {}\n" +
                "🌐 网络访问: {}\n" +
                "📚 API文档: http://localhost:{}/swagger-ui.html\n",
                applicationName, localUrl, networkUrl, port
            );
            
        } catch (UnknownHostException e) {
            logger.error("获取主机地址失败", e);
            
            // 简化版本的启动信息
            logger.info("\n🎉 {} 启动成功！\n📍 访问地址: http://localhost:{}{}\n📚 API文档: http://localhost:{}/swagger-ui.html\n",
                applicationName, port, contextPath, port
            );
        }
    }
}