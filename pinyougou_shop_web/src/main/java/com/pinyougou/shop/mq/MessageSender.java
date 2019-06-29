package com.pinyougou.shop.mq;

import com.alibaba.fastjson.JSON;
import entity.MessageInfo;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {
    @Autowired
    private DefaultMQProducer producer;

    /**
     * 跟据内容发送消息
     * @param info
     */
    public void sendMessage(MessageInfo info){
        try {
            //把消息对象转换成json串发送内容
            String content = JSON.toJSONString(info);
            Message message = new Message(
                    info.getTopic(), //主题名称
                    info.getTags(),  //标签名称
                    info.getKeys(),  //key
                    content.getBytes() //发送的内容
            );
            producer.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}