package com.sto.autoservice.core.repository;

import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /** Поиск по логину ИЛИ email (для авторизации). */
    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Активные (не заблокированные и не удалённые) пользователи роли, например — список мастеров. */
    List<User> findByRoleAndBlockedFalseAndDeletedFalseOrderByFullNameAsc(Role role);

    Optional<User> findByIdAndRoleAndBlockedFalseAndDeletedFalse(Long id, Role role);
}
