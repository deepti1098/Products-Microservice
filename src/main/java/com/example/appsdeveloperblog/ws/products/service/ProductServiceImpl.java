package com.example.appsdeveloperblog.ws.products.service;

import com.example.appsdeveloperblog.ws.core.ProductCreatedEvent;
import com.example.appsdeveloperblog.ws.products.rest.CreateProductRestModel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ProductServiceImpl implements ProductService {
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String createProduct(CreateProductRestModel productRestModel) throws Exception{
        String productId = UUID.randomUUID().toString();

        // TODO: Persist Product Details into database table before publishing an Event

        ProductCreatedEvent productCreatedEvent= new ProductCreatedEvent(productId, productRestModel.getTitle(),
                                                        productRestModel.getPrice(), productRestModel.getQuantity());

        // to send synchronously(means waiting from acknowledgement from kafka broker that message is being published) only do following
        logger.info("Before publishing a ProductCreatedEvent");

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>("product-created-events-topic",productId, productCreatedEvent);
        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());
        SendResult<String, ProductCreatedEvent> result =
                kafkaTemplate.send(record).get();

        logger.info("Partition: " + result.getRecordMetadata().partition());
        logger.info("Topic: " + result.getRecordMetadata().topic());
        logger.info("Offset: " + result.getRecordMetadata().offset());

        logger.info("****** Returning product Id"); //So in case of asynchronous , this message should be printed first and only after that successful message will be printed next. if sends product created event works asynchronously and does not wait for the result,
        return productId;
    }
}

// ***Note:
// to send asynchronously

//        CompletableFuture<SendResult<String , ProductCreatedEvent>> future =kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);
//
//        future.whenComplete((result, exception)->{
//            if(exception!=null){
//                logger.error("******Failed to send messages"+ exception.getMessage());
//            }
//            else{
//                logger.info("******Message send successfully" + result.getRecordMetadata());
//            }
//        });