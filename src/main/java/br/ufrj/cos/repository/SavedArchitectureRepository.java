package br.ufrj.cos.repository;

import br.ufrj.cos.domain.SavedArchitecture;
import br.ufrj.cos.domain.UserApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedArchitectureRepository extends JpaRepository<SavedArchitecture, Long> {
    List<SavedArchitecture> findByUserApplicationOrderByUpdatedAtDesc(UserApplication userApplication);
    List<SavedArchitecture> findByUserApplication_UserNameOrderByUpdatedAtDesc(String userName);
}
