package com.pinyougou.page.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import entity.EsItem;
import entity.MessageInfo;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.List;

/**
 * 商品详情更新-监听器
 * @author Steven
 * @version 1.0
 * @description com.itheima.mq.listener
 * @date 2019-6-6
 */
public class MessageListener implements MessageListenerConcurrently {
    @Autowired
    private ItemPageService itemPageService;
    @Value("${PAGE_DIR}")
    private String PAGE_DIR;

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
                    //生成详情页
                    for (EsItem esItem : esItemList) {
                        itemPageService.genItemHtml(esItem.getGoodsId());
                    }

                }else if(info.getMethod() == MessageInfo.METHOD_DELETE){  //删除操作
                    //转换id列表
                    List<Long> idArray = JSON.parseArray(info.getContext().toString(), Long.class);
                    //删除静态文件
                    for (Long goodsId : idArray) {
                        //删除文件
                        boolean result = new File(PAGE_DIR + goodsId + ".html").delete();
                        System.out.println("删除商品 "+ goodsId + " 静态页：" + result);
                    }

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
