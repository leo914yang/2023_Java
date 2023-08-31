package com.twnch.eachbatch.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PattenFinder {

    public String findMyPatten(String targetString, String matcherString){
        // 正規表達式?代表可以有也可以沒有
        // \d 表示匹配一個數字字符, +表示前面的元素可以出現一次或多次
        Pattern myPattern = Pattern.compile(targetString + "=(-?\\d+)");
        Matcher myMatcher = myPattern.matcher(matcherString);
        if(myMatcher.find())
            return myMatcher.group();
        else
            return null;
    }

}
