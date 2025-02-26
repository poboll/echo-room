package com.caiths.echoapi.entity;

import java.util.List;

/**
 * @author poboll
 * @date 2025/2/26 - 19:18
 */
public class RespPageBean {
  private Long total;//数据总数
  private List<?> data;//数据实体列表

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public List<?> getData() {
    return data;
  }

  public void setData(List<?> data) {
    this.data = data;
  }
}
