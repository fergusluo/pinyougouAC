package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.pay.service.impl
 * @date 2019-6-14
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${notifyurl}")
    private String notifyurl;
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //提取参数返回
        Map result = new HashMap();
        try {
            //1、组装统一下单的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众账号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            param.put("body", "品优购");  //商品描述，用户扫码后看到的支付信息
            param.put("out_trade_no", out_trade_no);  //商户订单号
            param.put("total_fee", total_fee);  //支付金额(分)
            param.put("spbill_create_ip", "127.0.0.1");  //终端IP，可以通过Controller使用request获取
            param.put("notify_url", notifyurl);  //通知地址，微信支付成功后回调的url
            //交易类型JSAPI -JSAPI支付 NATIVE -Native支付 APP -APP支付
            param.put("trade_type", "NATIVE");

            //生成带签名的xml字符串
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起微信统一下单接口，参数为：" + paramXml);
            //2、通过httpClient发起统一下单请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(paramXml); //传入请求参数
            httpClient.post();  //发起请求
            String resultXml = httpClient.getContent();
            System.out.println("发起微信统一下单接口成功，响应结果为：" + resultXml);
            //3、解析响应的结果,包装返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            result.put("out_trade_no", out_trade_no);  //返回订单号
            result.put("total_fee", total_fee);  //返回支付金额
            result.put("code_url", resultMap.get("code_url"));  //二维码连接内容
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1、组装统一下单的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众账号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            param.put("out_trade_no", out_trade_no);  //商户订单号

            //生成带签名的xml字符串
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起微信订单查询接口，参数为：" + paramXml);
            //2、通过httpClient发起统一下单请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(paramXml); //传入请求参数
            httpClient.post();  //发起请求
            String resultXml = httpClient.getContent();
            System.out.println("发起微信订单查询接口成功，响应结果为：" + resultXml);
            //3、解析响应的结果,包装返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        try {
            //1、组装统一下单的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众账号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            param.put("out_trade_no", out_trade_no);  //商户订单号

            //生成带签名的xml字符串
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起微信订单关闭接口，参数为：" + paramXml);
            //2、通过httpClient发起统一下单请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(paramXml); //传入请求参数
            httpClient.post();  //发起请求
            String resultXml = httpClient.getContent();
            System.out.println("发起微信订单关闭接口成功，响应结果为：" + resultXml);
            //3、解析响应的结果,包装返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
