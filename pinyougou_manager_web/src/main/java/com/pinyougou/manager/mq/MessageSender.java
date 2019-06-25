package com.pinyougou.manager.mq;

import com.alibaba.fastjson.JSON;
import entity.MessageInfo;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * 消息发送工具
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.managr.mq
 * @date 2019-6-6
 */
@Component
public class MessageSender {
    @Autowired
    private DefaultMQProducer producer;

    public void sendMessage(MessageInfo info){
        try {
            //传递的消息
            String json = JSON.toJSONString(info);
            Message message = new Message(
                    info.getTopic(),
                    info.getTags(),
                    info.getKeys(),
                    json.getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            //发送消息
            producer.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
