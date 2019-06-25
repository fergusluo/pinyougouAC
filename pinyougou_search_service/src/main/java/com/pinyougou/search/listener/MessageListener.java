package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.search.service.ItemSearchService;
import entity.EsItem;
import entity.MessageInfo;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 索引库更新-监听器
 * @author Steven
 * @version 1.0
 * @description com.itheima.mq.listener
 * @date 2019-6-6
 */
public class MessageListener implements MessageListenerConcurrently {
    @Autowired
    private ItemSearchService itemSearchService;

    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            //4.1.循环读取消息-msgs.for
            for (MessageExt msg : msgs) {
                //读取消息
                String json = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                //把消息内容转换为对象
                MessageInfo info = JSON.parseObject(json, MessageInfo.class);
                //如果新增操作
                if(info.getMethod() == MessageInfo.METHOD_ADD){
                    //把内容转换加来
                    List<EsItem> esItemList = JSON.parseArray(info.getContext().toString(), EsItem.class);
                    //增加索引库
                    itemSearchService.importList(esItemList);
                }else if(info.getMethod() == MessageInfo.METHOD_DELETE){  //删除操作
                    //转换id列表
                    List<Long> idArray = JSON.parseArray(info.getContext().toString(), Long.class);
                    //list转数据
                    Long[] ids = new Long[idArray.size()];
                    idArray.toArray(ids);
                    //删除索引库
                    itemSearchService.deleteByGoodsId(ids);
                }
                //判断场景处理相应的结果
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        //已经消息成功，下次不会再读取
        //4.2.返回消息读取状态-CONSUME_SUCCESS
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
