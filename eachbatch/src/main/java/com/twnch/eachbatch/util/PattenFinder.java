package com.twnch.eachbatch.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PattenFinder {

    public String findMyPatten(String targetString, String matcherString){
        Pattern myPattern = Pattern.compile(targetString + "=(-?\\d+)");
        Matcher myMatcher = myPattern.matcher(matcherString);
        if(myMatcher.find())
            return myMatcher.group();
        else
            return null;
    }

}
