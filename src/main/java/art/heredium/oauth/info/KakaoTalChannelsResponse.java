package art.heredium.oauth.info;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class KakaoTalChannelsResponse {
    private Long user_id;
    private List<Channels> channels;

    @Getter
    @Setter
    @ToString
    public static class Channels {
        private String channel_uuid;
        private String channel_public_id;
        private String relation;
        private LocalDateTime updated_at;
    }
}
