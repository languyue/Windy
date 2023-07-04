package com.zj.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum ProcessStatus {
  SUCCESS(1, "处理成功"),
  FAIL(2, "处理失败"),
  TIMEOUT(3, "处理超时"),
  RUNNING(4, "运行中"),
  IGNORE_FAIL(5, "忽略失败"),
  STOP(6, "停止运行"),
  ;

  ProcessStatus(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public static boolean isCompleteStatus(Integer status) {
    return !Objects.equals(status, ProcessStatus.RUNNING.getType());
  }

  public static boolean isSuccess(Integer status) {
    return Objects.equals(status, ProcessStatus.SUCCESS.getType());
  }

  public boolean isFailStatus() {
    List<Integer> list = Arrays.asList(ProcessStatus.FAIL.getType(),
        ProcessStatus.TIMEOUT.getType(), ProcessStatus.STOP.getType());
    return list.contains(this.getType());
  }

  public boolean isSuccess() {
    return Objects.equals(this.type, ProcessStatus.SUCCESS.getType());
  }

  public static ProcessStatus exchange(Integer type) {
    return Arrays.stream(ProcessStatus.values())
        .filter(status -> Objects.equals(status.getType(), type)).findFirst().orElse(null);
  }

  private final int type;
  private final String desc;

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }
}
