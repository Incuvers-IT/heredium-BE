package art.heredium.core.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Smaller numbers have higher priority. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterOrder {
  public static final int HTTP_LOG_FILTER = 1;
}
