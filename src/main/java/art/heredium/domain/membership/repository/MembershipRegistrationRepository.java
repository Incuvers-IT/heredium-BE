package art.heredium.domain.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.membership.entity.MembershipRegistration;

public interface MembershipRegistrationRepository
    extends JpaRepository<MembershipRegistration, Long> {}
