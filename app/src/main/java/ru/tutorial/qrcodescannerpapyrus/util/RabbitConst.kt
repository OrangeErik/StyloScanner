package ru.tutorial.qrcodescannerpapyrus.util
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