package art.heredium.domain.common.type;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum FilePathType {
  TEMP(0, "임시 저장", "temp", null, 10),
  EDITOR(1, "에디터 이미지 임시 저장", "temp", Arrays.asList("png", "jpg", "jpeg", "gif"), 5),
  POPUP(2, "팝업 파일", "popup", null, null),
  SLIDE(3, "슬라이드 파일", "slide", null, null),
  NOTICE(4, "공지사항 파일", "notice", null, null),
  POLICY(5, "약관 파일", "policy", null, null),
  EVENT(6, "이벤트", "event", null, null),
  EXHIBITION(7, "전시 파일", "exhibition", null, null),
  PROGRAM(8, "프로그램 파일", "program", null, null),
  COFFEE(9, "커피", "coffee", null, null),
  DOCENT(10, "도슨트 파일", "docent", null, null),
  MEMBERSHIP(12, "멤버십 파일", "membership", null, null),
  COUPON(13, "쿠폰 파일", "coupon", null, null),
  ;

  private int code;
  private String desc;
  private String path;
  private List<String> extension;
  private Integer megabytes;

  FilePathType(int code, String desc, String path, List<String> extension, Integer megabytes) {
    this.code = code;
    this.path = path;
    this.desc = desc;
    this.extension = extension;
    this.megabytes = megabytes;
  }

  public static FilePathType ofCode(int code) {
    FilePathType find =
        Arrays.stream(FilePathType.values())
            .filter(v -> v.getCode() == code)
            .findAny()
            .orElseThrow(
                () -> new RuntimeException(String.format("상태코드에 code=[%d]가 존재하지 않습니다", code)));
    return find;
  }
}
