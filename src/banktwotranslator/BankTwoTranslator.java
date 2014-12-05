package banktwotranslator;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import dk.cphbusiness.connection.ConnectionCreator;
import java.io.IOException;

/**
 *
 * @author mhck
 */
public class BankTwoTranslator {
    private static final String BANKEXCHANGE_NAME = "cphbusiness.bankXML";
    private static final String REPLY_QUEUE = "bank_one_normalizer";
    private static final String QUEUE_NAME = "xml_translator_two";

    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionCreator cc = ConnectionCreator.getInstance();
        Channel channelIn = cc.createChannel();
        Channel channelOut = cc.createChannel();
        channelIn.queueDeclare(QUEUE_NAME, true, false, false, null);
        QueueingConsumer consumer = new QueueingConsumer(channelIn);
        channelIn.basicConsume(QUEUE_NAME, true, consumer);
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = translateMessage(delivery);
            System.out.println(message);
            BasicProperties probs = new BasicProperties.Builder().replyTo(REPLY_QUEUE).build();
            channelOut.basicPublish(BANKEXCHANGE_NAME, "", probs, message.getBytes());
        }
    }
    
    private static String translateMessage(QueueingConsumer.Delivery delivery) {
        String message = new String(delivery.getBody());
        return DateCalculator.translateDate(message);
    }
}