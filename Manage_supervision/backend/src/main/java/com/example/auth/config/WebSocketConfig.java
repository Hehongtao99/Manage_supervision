package com.example.auth.config;

import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    
    private final JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，添加SockJS支持
        logger.info("注册WebSocket端点: /ws");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 允许所有来源
                .setAllowedOrigins("http://localhost:5173")  // 明确添加Vite开发服务器
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")  // 使用CDN上的SockJS客户端库
                .setWebSocketEnabled(true)  // 确保WebSocket传输启用
                .setSessionCookieNeeded(false);  // 禁用会话cookie，简化开发
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理
        logger.info("配置WebSocket消息代理");
        
        // 客户端发送消息的目的地前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 服务器向客户端推送消息的目的地前缀
        registry.enableSimpleBroker("/user", "/topic");
        // 为点对点通信设置前缀
        registry.setUserDestinationPrefix("/user");
        
        logger.info("WebSocket消息代理配置完成");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        logger.info("配置WebSocket客户端入站通道拦截器");
        
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    logger.info("收到STOMP CONNECT命令");
                    String token = accessor.getFirstNativeHeader("Authorization");
                    
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        logger.info("从WebSocket连接中提取到JWT Token: {}", token.substring(0, Math.min(10, token.length())) + "...");
                        
                        // 验证Token
                        if (jwtUtil.validateToken(token)) {
                            try {
                                // 从token提取用户ID
                                Long userId = jwtUtil.getUserIdFromToken(token);
                                logger.info("从JWT Token提取的用户ID: {}", userId);
                                
                                if (userId != null) {
                                    // 设置用户ID为Principal名称
                                    final Long finalUserId = userId;
                                    accessor.setUser(() -> String.valueOf(finalUserId));
                                    logger.info("WebSocket连接成功设置用户ID为Principal: {}", userId);
                                } else {
                                    logger.error("WebSocket连接失败: Token中未包含有效的用户ID");
                                    return null; // 拒绝连接
                                }
                            } catch (Exception e) {
                                logger.error("WebSocket连接设置Principal失败: {}", e.getMessage(), e);
                                return null; // 拒绝连接
                            }
                        } else {
                            logger.error("WebSocket连接JWT验证失败，Token无效");
                            return null; // 拒绝连接
                        }
                    } else {
                        logger.error("WebSocket连接缺少Authorization头或格式不正确");
                        return null; // 拒绝连接
                    }
                } else if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    Principal user = accessor.getUser();
                    if (user == null) {
                        logger.error("消息中缺少用户身份信息，拒绝消息: {}", accessor.getCommand());
                        return null;
                    }
                }
                return message;
            }
        });
        
        logger.info("WebSocket客户端入站通道拦截器配置完成");
    }
} 