package com.qik.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * RandomController
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/6/26 12:26
 **/
@RestController
@RequestMapping("random")
public class RandomController {
    @GetMapping("/int/{from}/{to}")
    public List<Integer> random(@PathVariable("from") int from, @PathVariable("to") int to) {
        List<Integer> returnList = new ArrayList<>(5);
        Random random = new Random();
        for(int i=0;i<5;i++) {
            returnList.add(random.nextInt(to)+1);
        }
        Collections.sort(returnList);
        return returnList;
    }
}
