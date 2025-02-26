package com.caiths.echoapi.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author poboll
 * @date 2025/2/26 - 16:28
 */
@Component
public class DateConverter implements Converter<String, Date> {

  SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  @Override
  public Date convert(String s) {
    Date date = null;
    if (s!=null&&!"".equals(s)){
      try {
       date= simpleDateFormat.parse(s);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return date;
  }
}
