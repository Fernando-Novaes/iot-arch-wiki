package br.ufrj.cos.repository;

import br.ufrj.cos.domain.Architecture;
import br.ufrj.cos.domain.QualityRequirementTechnology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface QualityRequirementTechnologyRepository extends JpaRepository<QualityRequirementTechnology, Long> {
}
