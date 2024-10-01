package art.heredium.domain.common.type;

import java.time.LocalDateTime;

import lombok.Getter;

import art.heredium.core.util.Constants;
import art.heredium.domain.common.converter.GenericTypeConverter;

@Getter
public enum ProjectStateType implements PersistableEnum<Integer> {
  SCHEDULE(0, "예정"),
  BOOKING(1, "예매"),
  PROGRESS(2, "진행"),
  TERMINATION(3, "종료"),
  ;

  private int code;
  private String desc;

  ProjectStateType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<ProjectStateType, Integer> {
    public Converter() {
      super(ProjectStateType.class);
    }
  }

  public static ProjectStateType getState(
      LocalDateTime startDate, LocalDateTime endDate, LocalDateTime bookingDate) {
    LocalDateTime now = Constants.getNow();
    if (now.isBefore(startDate) && now.isBefore(bookingDate)) {
      return ProjectStateType.SCHEDULE;
    } else if (!now.isBefore(bookingDate) && now.isBefore(startDate)) {
      return ProjectStateType.BOOKING;
    } else if (!now.isBefore(startDate) && !now.isAfter(endDate)) {
      return ProjectStateType.PROGRESS;
    } else if (now.isAfter(endDate)) {
      return ProjectStateType.TERMINATION;
    }
    return null;
  }
}
