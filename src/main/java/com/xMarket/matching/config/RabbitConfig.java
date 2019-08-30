package com.xMarket.matching.config;


import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.amqp.core.Binding;
        import org.springframework.amqp.core.BindingBuilder;
        import org.springframework.amqp.core.DirectExchange;
        import org.springframework.amqp.core.Queue;
        import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
        import org.springframework.amqp.rabbit.connection.ConnectionFactory;
        import org.springframework.amqp.rabbit.core.RabbitTemplate;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.beans.factory.config.ConfigurableBeanFactory;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.context.annotation.Scope;

@Configuration
public class RabbitConfig {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;


    public static final String EXCHANGE_A = "marchExchange";
    public static final String EXCHANGE_B = "tradeOrderExchange";
//    public static final String EXCHANGE_C = "my-mq-exchange_C";


    public static final String QUEUE_A = "marchQueue";//连续竞价
    public static final String QUEUE_B = "cancelOrderQueue";//撤单
    public static final String QUEUE_C = "tradeOrderQueue";//成交单
    public static final String QUEUE_D = "allMarchQueue";//集合竞价

    public static final String ROUTINGKEY_A = "marchRoutingKey";
    public static final String ROUTINGKEY_B = "cancelOrderRoutingKey";
    public static final String ROUTINGKEY_C = "tradeOrderRoutingKey";
    public static final String ROUTINGKEY_D = "allMarchRoutingKey";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }
//
//    @Bean
//    public DirectExchange defaultExchange() {
//        return new DirectExchange(EXCHANGE_A);
//    }
//    @Bean
//    public Queue queueA() {
//        return new Queue(QUEUE_A, true); //队列持久
//    }
//
//    @Bean
//    public Queue queueB() {
//        return new Queue(QUEUE_B, true); //队列持久
//    }
//
//    @Bean
//    public Queue queueC() {
//        return new Queue(QUEUE_C, true); //队列持久
//    }
//
//    @Bean
//    public Queue queueD() {
//        return new Queue(QUEUE_D, true); //队列持久
//    }
//
//    @Bean
//    public Binding binding() {
//
//        return BindingBuilder.bind(queueA()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_A);
//    }
//    @Bean
//    public Binding bindingC(){
//        return BindingBuilder.bind(queueC()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_C);
//    }
//
//    @Bean
//    public Binding bindingB(){
//        return BindingBuilder.bind(queueB()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_B);
//    }
//
//    @Bean
//    public Binding bindingD(){
//        return BindingBuilder.bind(queueB()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_D);
//    }
}
