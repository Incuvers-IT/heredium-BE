package art.heredium.domain.common.type;

import lombok.Getter;

@Getter
public enum ResizeImageType {
  PROJECT_THUMBNAIL(
      0, "전시 썸네일", new int[] {1920, 1320}, new int[] {1294, 889}, new int[] {640, 440}),
  EVENT_THUMBNAIL(1, "이벤트 썸네일", new int[] {1920, 632}, new int[] {1642, 540}, new int[] {668, 220}),
  PROJECT_DETAIL_IMAGE(3, "전시 상세 이미지", new int[] {1920, 800}, null, null),
  SMALL(2, "small", null, null, new int[] {400, 400}),
  ;

  private int code;
  private String desc;
  private int[] small;
  private int[] medium;
  private int[] large;

  ResizeImageType(int code, String desc, int[] large, int[] medium, int[] small) {
    this.code = code;
    this.desc = desc;
    this.large = large;
    this.medium = medium;
    this.small = small;
  }
}
