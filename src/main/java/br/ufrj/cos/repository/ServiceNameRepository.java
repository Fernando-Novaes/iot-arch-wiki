package br.ufrj.cos.repository;

import br.ufrj.cos.domain.ServiceName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceNameRepository extends JpaRepository<ServiceName, Long> { // Use Long or your actual ID type
}
