package banktwotranslator;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import java.io.IOException;

/**
 *
 * @author mhck
 */
public class XMLTranslator {
    private static final String BANKEXCHANGE_NAME = "cphbusiness.bankXML";
    private static final String REPLY_QUEUE = "bank_one_normalizer";
    private static final String EXCHANGE_NAME = "translator_exchange_topic";
    private static final String QUEUE_NAME = "xml_translator_two";
    private static final String[] TOPICS = {"expensive.*"};

    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("nicklas");
        factory.setPassword("cph");
        factory.setHost("datdb.cphbusiness.dk");

        Connection connection = factory.newConnection();
        Channel channelIn = connection.createChannel();
        Channel channelOut = connection.createChannel();
        channelIn.queueDeclare(QUEUE_NAME, true, false, false, null);
        channelIn.exchangeDeclare(EXCHANGE_NAME, "topic");
        
        for(String topic: TOPICS){
            channelIn.queueBind(QUEUE_NAME, EXCHANGE_NAME,topic);
        }
        
        QueueingConsumer consumer = new QueueingConsumer(channelIn);
        channelIn.basicConsume(QUEUE_NAME, true, consumer);
//        String testMessage = "{\"ssn\":1605789787,\"loanAmount\":10.0,\"loanDuration\":360,\"rki\":false}"; //test sender besked til sig selv.
//        String testMessage = "{\"ssn\":1605789787,\"creditScore\":598,\"loanAmount\":10.0,\"loanDuration\":360}";
//        channel.basicPublish("", QUEUE_NAME, null, testMessage.getBytes()); // test

        while (true) {
            Delivery delivery = consumer.nextDelivery();
            //channelIn.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            //System.out.println(new String(delivery.getBody()));
            String message = translateMessage(delivery);
            System.out.println(message);
            BasicProperties probs = new BasicProperties.Builder().replyTo(REPLY_QUEUE).correlationId("1").build();
            channelOut.basicPublish(BANKEXCHANGE_NAME, "", probs, message.getBytes());
        }
    }
    
    private static String translateMessage(QueueingConsumer.Delivery delivery) {
        String message = new String(delivery.getBody());
        return DateCalculator.translateDate(message);
    }
}