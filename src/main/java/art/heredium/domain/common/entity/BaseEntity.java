package art.heredium.domain.common.entity;

import art.heredium.domain.account.entity.UserPrincipal;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(name = "created_name", nullable = false, updatable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT ''")
    private String createdName;

    @LastModifiedBy
    @Column(name = "last_modified_name", nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT ''")
    private String lastModifiedName;

    public void updateLastModifiedName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return;
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        this.lastModifiedName = principal.getName();
    }
}
