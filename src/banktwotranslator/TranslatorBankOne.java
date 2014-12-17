package banktwotranslator;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import dk.cphbusiness.connection.ConnectionCreator;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import utilities.xml.xmlMapper;

/**
 *
 * @author mhck
 */
public class TranslatorBankOne {

    private static final String BANKEXCHANGE_NAME = "cphbusiness.bankXML";
    private static final String REPLY_QUEUE = "bank_one_normalizer_gr1";
    private static final String EXCHANGE_NAME = "ex_translators_gr1";
    private static final String INQUEUE_NAME = "xml_translator_two_gr1";
    private static final String[] TOPICS = {"expensive.high"};

    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionCreator creator = ConnectionCreator.getInstance();
        Channel channelIn = creator.createChannel();
        Channel channelOut = creator.createChannel();
        channelIn.queueDeclare(INQUEUE_NAME, true, false, false, null);
        channelIn.exchangeDeclare(EXCHANGE_NAME, "topic");

        for (String topic : TOPICS) {
            channelIn.queueBind(INQUEUE_NAME, EXCHANGE_NAME, topic);
        }

        QueueingConsumer consumer = new QueueingConsumer(channelIn);
        channelIn.basicConsume(INQUEUE_NAME, true, consumer);
        System.out.println("Translator for Bank One running");
        while (true) {    
            Delivery delivery = consumer.nextDelivery();
            System.out.println("Got message: " + new String(delivery.getBody()));
            String message = translateMessage(delivery);
            BasicProperties probs = new BasicProperties.Builder().replyTo(REPLY_QUEUE).correlationId("1").build();
            channelOut.basicPublish(BANKEXCHANGE_NAME, "", probs, message.getBytes());
        }
    }

    private static String translateMessage(QueueingConsumer.Delivery delivery) {
        String message = new String(delivery.getBody());
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = xmlMapper.getXMLDocument(message);
        try {
            String ssn = xPath.compile("/LoanRequest/ssn").evaluate(doc);
            ssn = ssn.replace("-", "");
            doc.getElementsByTagName("ssn").item(0).getFirstChild().setNodeValue(ssn);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(TranslatorBankOne.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DateCalculator.translateDate(message);
    }
}
