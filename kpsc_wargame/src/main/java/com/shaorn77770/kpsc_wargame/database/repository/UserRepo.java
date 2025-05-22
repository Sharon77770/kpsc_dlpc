package com.shaorn77770.kpsc_wargame.database.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.shaorn77770.kpsc_wargame.data_class.UserData;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Repository
@RequiredArgsConstructor
public class UserRepo {
    private final EntityManager em;
    private static final Logger logger = LogManager.getLogger(UserRepo.class);

    public void insert(UserData user) {
        try {
            em.persist(user);
            logger.info("유저 insert: {}", user.getApiKey());
        } catch (Exception e) {
            logger.error("유저 insert 예외 발생: {}", user.getApiKey(), e);
            throw e;
        }
    }

    public void update(UserData user) {
        try {
            em.merge(user);
            logger.info("유저 update: {}", user.getApiKey());
        } catch (Exception e) {
            logger.error("유저 update 예외 발생: {}", user.getApiKey(), e);
            throw e;
        }
    }
    
    public void remove(String userId) {
        try {
            em.remove(findById(userId));
            logger.info("유저 remove: {}", userId);
        } catch (Exception e) {
            logger.error("유저 remove 예외 발생: {}", userId, e);
            throw e;
        }
    }
    
    public UserData findById(String userId) {
        try {
            return em.find(UserData.class, userId);
        } catch (Exception e) {
            logger.error("findById 예외 발생: {}", userId, e);
            throw e;
        }
    }
    
    public Boolean contains(String userId) {
        return !em.createQuery("SELECT u FROM UserData u WHERE u.apiKey = :targetName", UserData.class)
                .setParameter("targetName", userId)
                .getResultList().isEmpty();
    }

    public List<UserData> notAllowedUsers() {
        return em.createQuery("SELECT u FROM UserData u WHERE u.allow = False", UserData.class)
                .getResultList();
    }

    public List<UserData> allowedUsers() {
        return em.createQuery("SELECT u FROM UserData u WHERE u.allow = True", UserData.class)
                .getResultList();
    }
}
