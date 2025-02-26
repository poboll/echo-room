package com.caiths.echoapi.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import com.caiths.echoapi.entity.User;

/**
 * @author poboll
 * @date 2025/2/26 - 22:56
 * 用户工具类
 */
public class UserUtil {
  /**
   * 获取当前登录用户实体
   * @return
   */
  public static User getCurrentUser(){
    return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }
}
