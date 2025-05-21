package com.shaorn77770.kpsc_wargame.database.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.repository.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepo userRepo;

    public void save(UserData user) {
        if(userRepo.contains(user.getApiKey()))
            userRepo.update(user);
        else
            userRepo.insert(user);
    }

    public void remove(String id) {
        userRepo.remove(id);
    }

    public UserData findByKey(String key) {
        return userRepo.findById(key);
    }

    public boolean contains(String key) {
        return userRepo.contains(key);
    }

    public List<UserData> getNotAllowedUsers() {
        return userRepo.notAllowedUsers();
    }

    public List<UserData> getAllowedUsers() {
        return userRepo.allowedUsers();
    }
}
