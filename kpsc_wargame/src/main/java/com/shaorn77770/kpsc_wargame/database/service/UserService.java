package com.shaorn77770.kpsc_wargame.database.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.shaorn77770.kpsc_wargame.data_class.UserData;
import com.shaorn77770.kpsc_wargame.database.repository.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserRepo userRepo;

    public void save(UserData user) {
        try {
            if(userRepo.contains(user.getApiKey()))
                userRepo.update(user);
            else
                userRepo.insert(user);
            logger.info("유저 저장/업데이트: {}", user.getApiKey());
        } catch (Exception e) {
            logger.error("유저 저장/업데이트 예외 발생: {}", user.getApiKey(), e);
            throw e;
        }
    }

    public void remove(String id) {
        try {
            userRepo.remove(id);
            logger.info("유저 삭제: {}", id);
        } catch (Exception e) {
            logger.error("유저 삭제 예외 발생: {}", id, e);
            throw e;
        }
    }

    public UserData findByKey(String key) {
        try {
            return userRepo.findById(key);
        } catch (Exception e) {
            logger.error("findByKey 예외 발생: {}", key, e);
            throw e;
        }
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
