package art.heredium.core.jwt;

import java.time.Duration;

import lombok.AllArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtRedisUtil {

  private final RedisTemplate<String, Object> redisTemplate;

  public <T> T getData(String key, Class<T> aClass) {
    Object value = redisTemplate.opsForValue().get(key);
    return value == null ? null : aClass.cast(value);
  }

  public String getData(String key) {
    return getData(key, String.class);
  }

  public void setData(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public void setDataExpire(String key, Object value, long duration) {
    Duration expireDuration = Duration.ofSeconds(duration);
    redisTemplate.opsForValue().set(key, value, expireDuration);
  }

  public void deleteData(String key) {
    redisTemplate.delete(key);
  }

  public Long getDataExpire(String key) {
    return redisTemplate.getExpire(key);
  }
}
