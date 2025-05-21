package com.shaorn77770.kpsc_wargame.database.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.shaorn77770.kpsc_wargame.data_class.UserData;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepo {
    private final EntityManager em;

    public void insert(UserData user) {
        em.persist(user);
    }

    public void update(UserData user) {
        em.merge(user);
    }
    
    public void remove(String userId) {
        em.remove(findById(userId));
    }
    
    public UserData findById(String userId) {
        return em.find(UserData.class, userId);
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
