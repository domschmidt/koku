package de.domschmidt.koku.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UserRepository extends JpaRepository<User, String>, QuerydslPredicateExecutor<User> {

}
