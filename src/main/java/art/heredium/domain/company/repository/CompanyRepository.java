package art.heredium.domain.company.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.company.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  Optional<Company> findByName(final String name);
}
