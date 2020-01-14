package com.atguigu.gmall0715.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.PaymentInfo;
import com.atguigu.gmall0715.bean.enums.PaymentStatus;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.payment.config.AlipayConfig;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.alipay.api.AlipayConstants.*;
import static org.apache.catalina.manager.Constants.CHARSET;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;


    //http://payment.gmall.com/index?orderId=123
    @RequestMapping("index")
    @LoginRequire(autoRedirect = false)
    public String index(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        request.setAttribute("orderId", orderId);
        //通过orderId查询订单总金额
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }


    /**
     * 保存订单并返回二维码
     * http://payment.gmall.com/alipay/submit
     *
     * @return
     */
    @RequestMapping("alipay/submit")
    @ResponseBody // 将数据直接渲染到页面 !
    public String alipaySubmit(HttpServletRequest request,HttpServletResponse response) {
        //1.保存交易记录
        //1.1 获取订单id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject("啤酒饮料矿泉水，花生瓜子八宝粥。横批: 来腿让一让啊!");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentService.savePaymentInfo(paymentInfo);
        //2.生成二维码
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //设置 同步回调的url!
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //设置 异步回调的url
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        //将参数封装到alipayRequest 做成二维码 Json字符串
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject","啤酒饮料矿泉水，花生瓜子八宝粥。横批: 来腿让一让啊!");
        alipayRequest.setBizContent(JSON.toJSONString(map));
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }" +
//                "  }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + CHARSET);
        //直接将完整的表单html输出到页面
//        response.getWriter().write(form);
//        response.getWriter().flush();
//        response.getWriter().close();

        return form;
    }

    /**
     *  支付之后重定向到订单的Url
     * @return
     */
    @RequestMapping("alipay/callback/return")
    public String callBack(){
        //回调到订单页面
        return "redirect:"+AlipayConfig.return_order_url;
    }

    /**
     * 异步回调
     *  需要将中的参数： http://xxx.xxx.xxx/index?userId=123 封装为map
     * @return
     */
    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String notifyUrl(@RequestParam Map<String,String> paramMap,HttpServletRequest request) throws AlipayApiException {
        System.out.println("让一下谢谢!");
        //得到返回的状态
        String trade_status = paramMap.get("trade_status");
        //
        String out_trade_no = paramMap.get("out_trade_no");
        //做支付宝异步回调时总的思路为配置
        //只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED时，支付宝才会认定为买家付款成功
        //Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, CHARSET, AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED时".equals(trade_status)){
                // 如果paymentInfo中 payment_status是PAID 或者close 那么这个时候，也应该是失败的!
                //根据out_trade_no查询paymentInfo对象
                //select * from payment_info where out_trade_no = ?
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                if(paymentInfoQuery.getPaymentStatus() == PaymentStatus.PAID || paymentInfoQuery.getPaymentStatus() == PaymentStatus.ClOSED){
                    return "failure";
                }
                //跟新交易记录的状态，改为付款! payment_info
                // update paymentInfo set payment_status = PAID where out_trade_no = ?
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

    /**
     * 退款业务: 根据订单id实现退款
     *  http://payment.gmall.com/refund?orderId=xxx
     * @param orderId
     * @return
     */
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);
        return "" + flag;
    }

}
