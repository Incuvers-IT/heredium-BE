package art.heredium.domain.common.type;

import java.time.LocalDateTime;

import lombok.Getter;

import art.heredium.core.util.Constants;

@Getter
public enum DateState {
  PROGRESS(0, "진행"),
  SCHEDULE(1, "예정"),
  TERMINATION(2, "종료"),
  ;

  private int code;
  private String desc;

  DateState(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static DateState getState(LocalDateTime startDate, LocalDateTime endDate) {
    LocalDateTime now = Constants.getNow();
    if (now.isBefore(startDate)) {
      return SCHEDULE;
    } else if (now.isAfter(endDate)) {
      return TERMINATION;
    } else {
      return PROGRESS;
    }
  }

  public static DateState getState(LocalDateTime startDate) {
    LocalDateTime now = Constants.getNow();
    if (now.isBefore(startDate)) {
      return SCHEDULE;
    } else {
      return PROGRESS;
    }
  }
}
