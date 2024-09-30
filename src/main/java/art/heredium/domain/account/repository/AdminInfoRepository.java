package art.heredium.domain.account.repository;

import art.heredium.domain.account.entity.AdminInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminInfoRepository extends JpaRepository<AdminInfo, Long> {

    @Query("SELECT a.email FROM AdminInfo ai INNER JOIN Admin a ON a.id = ai.id WHERE ai.phone = :phone")
    List<String> findEmailByPhone(@Param("phone") String phone);
}
