package art.heredium.domain.ticket.model;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.NonUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketUserInfo {
    private Long accountId;
    private Long nonUserId;
    private Boolean isHanaBank;
    private String name;
    private String email;
    private String phone;
    private String password;

    public TicketUserInfo(Account account) {
        this.accountId = account.getId();
        this.name = account.getAccountInfo().getName();
        this.email = account.getEmail();
        this.phone = account.getAccountInfo().getPhone();
    }

    public TicketUserInfo(NonUser nonUser, String password) {
        this.nonUserId = nonUser.getId();
        this.name = nonUser.getName();
        this.email = nonUser.getEmail();
        this.phone = nonUser.getPhone();
        this.password = password;
        this.isHanaBank = nonUser.isHanaBank();
    }
}