package br.ufrj.cos.repository;


import br.ufrj.cos.domain.Architecture;
import br.ufrj.cos.domain.ArchitectureSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchitectureRepository extends JpaRepository<Architecture, Long> {

    @Query("SELECT a FROM Architecture a ORDER By a.name")
    List<Architecture> findAllOrderByName();

    List<Architecture> findByNameContainingIgnoreCase(String name);

}
