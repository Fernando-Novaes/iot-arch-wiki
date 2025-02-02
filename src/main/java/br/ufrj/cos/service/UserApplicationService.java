package br.ufrj.cos.service;

import br.ufrj.cos.domain.UserApplication;
import br.ufrj.cos.repository.UserApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserApplicationService {

    private final PasswordEncoder passwordEncoder;
    private final UserApplicationRepository userApplicationRepository;

    @Autowired
    public UserApplicationService(PasswordEncoder passwordEncoder, UserApplicationRepository userApplicationRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userApplicationRepository = userApplicationRepository;
    }

    public UserApplication save(UserApplication userApplication) {
        userApplication.setDateOfCreation(new Date());
        userApplication.setPassword(passwordEncoder.encode(userApplication.getPassword()));
        return userApplicationRepository.save(userApplication);
    }

    public UserApplication saveAndUpdate(UserApplication userApplication) {
        userApplication.setPassword(passwordEncoder.encode(userApplication.getPassword()));
        return userApplicationRepository.save(userApplication);
    }

    public void delete(UserApplication userApplication) {
        userApplicationRepository.delete(userApplication);
    }

    @Transactional(readOnly = true)
    public List<UserApplication> findAll() {
        return userApplicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserApplication> findByName(String name) {
        return userApplicationRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public UserApplication findByUserName(String userName) {
        return userApplicationRepository.findByUserName(userName);
    }
}

