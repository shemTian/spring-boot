package com.qik.demo.controller;

import com.alibaba.fastjson.JSON;
import com.qik.demo.util.EmailUtil;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("attendance")
public class AttendanceController {

  public static ConcurrentHashMap<String, String> emailMap = new ConcurrentHashMap<>();

  static {
    emailMap.put("UW000025", "liang.chen@upwild.cn");
    emailMap.put("UW000027", "haowenhai1990@163.com");
  }

  @Autowired
  RestTemplate restTemplate;

  private static String url = "http://c.upwild.cn/click/doClick";
  private static String token = "582F9E90F51127DA3520958C4599B58C9738B9B0DD754420740D16A8AEC67ABC4D0B90796D4CE70E63C2A0C15CDACE16";

  @RequestMapping("setToken")
  public String setToken(String tokenNew) {
    token = tokenNew;
    return "SUCCESS";
  }

  @RequestMapping("call")
  public String call(String a) {

    if (Objects.isNull(a)) {
      return "账户为空";
    }

    String cookies =
        "locale=zh; ua=Mozilla%2F5.0%20(iPhone%3B%20CPU%20iPhone%20OS%2012_2%20like%20Mac%20OS%20X)%20AppleWebKit%2F605.1.15%20(KHTML%2C%20like%20Gecko)%20Mobile%2F15E148%20wxwork%2F2.6.1%20MicroMessenger%2F6.3.22%20Language%2Fzh; route=33c6cbe6e0b769e4926b29fcd590025f; token="
            + token + "; wechatAccount=" + a;

    HttpHeaders headers = new HttpHeaders();
    MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
    headers.setContentType(type);
    headers.add("Content-Type", MediaType.APPLICATION_JSON.toString());
    headers.add("Accept", MediaType.APPLICATION_JSON.toString());
    headers.add("Cookie", cookies);
    headers.add("User-Agent",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Mobile Safari/537.36");
    HttpEntity<String> formEntity = new HttpEntity<>(
        "{\"ticket\":null,\"network_type\":\"wifi\",\"latitude\":31.280426025390625,\"longitude\":121.4225845336914}",
        headers);

    String message = restTemplate.postForObject(String.format(url), formEntity, String.class);
    MessageInfo messageInfo = JSON.parseObject(message, MessageInfo.class);
    if (messageInfo != null && Objects.equals(messageInfo.resultCode, 200)) {
      EmailUtil.sendEmail(emailMap.get(a), "", "X-X", "S", null);
    } else {
      EmailUtil.sendEmail(emailMap.get(a), "", "X-X", "F:" + messageInfo.getMessage(), null);
    }

    return "SUCCESS";
  }

  @RequestMapping("setEmail")
  public String setEmail(String a, String email) {

    if (Objects.isNull(a) || Objects.isNull(email)) {
      return "账户为空或邮箱为空";
    }

    emailMap.put(a, email);
    return "SUCCESS";
  }

  @Data
  static class MessageInfo implements Serializable {

    private Integer resultCode;
    private String message;
  }

  @Data
  static class UserInfo {

    private String account;
    private String email;
    private String userAgent;
    private String token;
  }

}
