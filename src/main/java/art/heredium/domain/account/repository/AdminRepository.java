package art.heredium.domain.account.repository;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.type.AuthType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long>, AdminRepositoryQueryDsl {

    @Override
    @EntityGraph(attributePaths = {"adminInfo"})
    Optional<Admin> findById(Long id);

    @EntityGraph(attributePaths = {"adminInfo"})
    Page<Admin> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"adminInfo"})
    Admin findByEmailEquals(String username);

    @EntityGraph(attributePaths = {"adminInfo"})
    Admin findTop1ByOrderById();

    boolean existsAdminByEmail(String email);

    boolean existsByEmailAndAdminInfo_Phone(String email, String accountInfo_phone);

    @Query("SELECT a.email FROM Admin a INNER JOIN AdminInfo ai ON a.id = ai.admin.id WHERE ai.auth = :authType AND ai.isEnabled IS TRUE")
    List<String> findAllEmailByAdminInfo_Auth(@Param("authType") AuthType authType);
}
