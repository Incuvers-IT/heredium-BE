package art.heredium.domain.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.membership.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {}
