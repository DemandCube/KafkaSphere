package kafkasphere;

import static spark.Spark.*;

import kafka.admin.AdminUtils;
import spark.*;

import java.util.Properties;
import java.util.Random;
import java.util.Date;
// Producer imports
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

// GET settings
import kafka.admin.AdminUtils.*;

/**
 * A RESTful server built on the Spark Framework (sparkjava.com).
 **/
public class GregorServer {
    public static void main(String[] args) {

        get(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                return "As Gregor Samsa awoke one morning from uneasy dreams he found himself transformed in his bed into a monstrous vermin...";
            }
        });
        get(new Route("/api/:topic") {
            @Override
            public Object handle(Request request, Response response) {
                String topic = request.params(":topic");

                SparkConsumerGroup simpleConsumer = new SparkConsumerGroup("localhost:2181", "test-consumer-group", topic);

                simpleConsumer.run(1);

                return topic;
            }
        });

        get(new Route("/api/monitor/:topic/") {
            @Override
            public Object handle(Request request, Response response) {
                String topic = request.params(":topic");
                return topic;
            }
        });

        post(new Route("/api/:topic") {
            @Override
            public Object handle(Request request, Response response) {
                String topic = request.params(":topic");
                String message = request.queryParams("message");

                // config the broker to send messages to
                Properties props = new Properties();
                props.put("metadata.broker.list", "localhost:9092");
                props.put("serializer.class", "kafka.serializer.StringEncoder");
                props.put("request.required.acks", "1");
                ProducerConfig config = new ProducerConfig(props);

                // initialize the producer
                Producer<String, String> producer = new Producer<String, String>(config);

                // craft a message:
                Random rnd = new Random();
                long runtime = new Date().getTime();
                String ip = "192.168.2." + rnd.nextInt(255);
                String msg = "[" + runtime + "] www.example.com - " + ip + "\nmessage:" +message;

                KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, ip, msg);

                producer.send(data);

                return "Success!\ntopic: "+topic+"\n" + msg+"\n";
            }
        });
    }
}