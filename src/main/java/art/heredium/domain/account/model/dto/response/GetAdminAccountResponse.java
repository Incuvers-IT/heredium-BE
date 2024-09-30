package art.heredium.domain.account.model.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminAccountResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private Boolean isMarketingReceive;
    private Long visitCount;

    @QueryProjection
    public GetAdminAccountResponse(Long id, String email, String name, String phone, LocalDateTime createdDate, LocalDateTime lastLoginDate, Boolean isMarketingReceive, Long visitCount) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.createdDate = createdDate;
        this.lastLoginDate = lastLoginDate;
        this.isMarketingReceive = isMarketingReceive;
        this.visitCount = visitCount;
    }
}