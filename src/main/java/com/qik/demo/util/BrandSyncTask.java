package com.qik.demo.util;

import com.qik.demo.controller.AttendanceController;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BrandSyncTask {

  @Autowired
  AttendanceController attendanceController;

  @Scheduled(cron = "0 10 19 * * ?")
  public void callNight() throws InterruptedException {

    Enumeration<String> accounts = AttendanceController.emailMap.keys();
    while (accounts.hasMoreElements()) {
      String account = accounts.nextElement();
      TimeUnit.SECONDS.sleep(new Random(10).nextInt(100));
      attendanceController.call(account);
    }
  }

  @Scheduled(cron = "0 30 9 * * ?")
  public void callMorning() throws InterruptedException {

    Enumeration<String> accounts = AttendanceController.emailMap.keys();
    while (accounts.hasMoreElements()) {
      String account = accounts.nextElement();
      TimeUnit.SECONDS.sleep(new Random(10).nextInt(100));
      attendanceController.call(account);
    }
  }
}