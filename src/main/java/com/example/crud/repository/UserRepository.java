package com.example.crud.repository;

import com.example.crud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User>, JpaRepository<User, Long> {
}
//public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User>, JpaRepository<User, Long> {
//}
