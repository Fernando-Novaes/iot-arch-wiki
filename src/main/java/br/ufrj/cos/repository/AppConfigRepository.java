package br.ufrj.cos.repository;

import br.ufrj.cos.domain.AppConfig;
import br.ufrj.cos.domain.Architecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
}
