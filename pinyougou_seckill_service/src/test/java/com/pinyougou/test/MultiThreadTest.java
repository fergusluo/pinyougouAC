package com.pinyougou.test;

import com.pinyougou.seckill.service.impl.MultiThreadWork;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.test
 * @date 2019-6-17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class MultiThreadTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MultiThreadWork multiThreadWork;

    @Test
    public void testDoSomething(){
        for (int i = 0; i < 5; i++) {
            System.out.println(i + "循环开始...");
            //开启了新线程去执行任务
            multiThreadWork.doSomething(i);
            System.out.println(i + "循环结束...");
        }
        try {
            //阻塞主线程，让主线程等待所有子线程执行结束
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
