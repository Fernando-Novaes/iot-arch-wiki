package br.ufrj.cos.service;

import br.ufrj.cos.domain.SavedArchitecture;
import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.repository.SavedArchitectureRepository;
import br.ufrj.cos.repository.UserApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SavedArchitectureService {

    private final SavedArchitectureRepository repository;
    private final UserApplicationRepository userRepository;

    public SavedArchitectureService(SavedArchitectureRepository repository, UserApplicationRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedArchitecture> findByUserName(String userName) {
        return repository.findByUserApplication_UserNameOrderByUpdatedAtDesc(userName);
    }

    @Transactional(readOnly = true)
    public Optional<SavedArchitecture> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public SavedArchitecture save(SavedArchitecture architecture, String userName) {
        if (architecture.getCreatedAt() == null) {
            architecture.setCreatedAt(LocalDateTime.now());
        }
        architecture.setUpdatedAt(LocalDateTime.now());

        if (architecture.getUserApplication() == null && userName != null) {
            UserApplication user = userRepository.findByUserName(userName);
            if (user != null) {
                architecture.setUserApplication(user);
            }
        }

        return repository.save(architecture);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
