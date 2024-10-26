package art.heredium.domain.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.membership.entity.CompanyMembershipRegistrationHistory;

public interface CompanyMembershipRegistrationHistoryRepository
    extends JpaRepository<CompanyMembershipRegistrationHistory, Long> {}
