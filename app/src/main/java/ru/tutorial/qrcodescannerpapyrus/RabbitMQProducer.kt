package ru.tutorial.qrcodescannerpapyrus

import com.rabbitmq.client.*

object RabbitConst {
	val STANDART_HOST = "localhost";
	val STANDART_PORT = "5672";
	val QUEUE_NAME:String = "new_durable_queue";
	var QUEUE_DURABLE = true;

	//Publish/Subscribe
	val PS_EXCHANGE_NAME = "logs";
	val PS_EXCHANGE_TYPE = "fanout";

	//Routing
	val R_EXCHANGE_NAME = "RoutingLogs";
	val R_EXCHANGE_TYPE = "direct";

	//Topic
	val T_EXCHANGE_NAME = "TopicsLogs";
	val T_EXCHANGE_TYPE = "topic";
}

//Routing Producer and main fun
class RoutingProducer(
	host: String = RabbitConst.STANDART_HOST,
	port: Int = RabbitConst.STANDART_PORT.toInt()) {

	val connectionFactory: ConnectionFactory;
	lateinit var connection:Connection;
	lateinit var channel:Channel;

	init {
		connectionFactory = ConnectionFactory()
		connectionFactory.host = host;
		connectionFactory.port = port;
		try {
			connection = connectionFactory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(RabbitConst.R_EXCHANGE_NAME, RabbitConst.R_EXCHANGE_TYPE);
		}catch (e:Exception) {
			connection.close();
			println(e);
			println("Hello Error. Error in channel and connection");
		}
	}

//	fun sendMsgs() {
//		val scanner = Scanner(System.`in`);
//		var message:String;
//		var logTag_RoutingKey:String;
//		while(true) {
//			println("Введите сообщение или exit() для выхода:");
//			message = scanner.nextLine();
//			if(message == "exit()")
//				break;
//			else {
//				println("Введите тег лога:");
//				logTag_RoutingKey = scanner.nextLine();
//				channel.basicPublish(RabbitConst.R_EXCHANGE_NAME, logTag_RoutingKey.toUpperCase(), null, message.toByteArray());
//				println(" [producer] Sent '$message'");
//			}
//		}
//		connection.close();
//	}
}