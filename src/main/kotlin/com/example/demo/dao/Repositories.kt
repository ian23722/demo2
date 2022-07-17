package com.example.demo.dao

import com.example.demo.domain.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

private const val FIND_USER_QUERY = """
        SELECT user_id, username, password, role, email 
        FROM user u 
        WHERE u.username = :username
    """

private const val FIND_CONFLICT_QUERY = """
        SELECT user_id, username, password, role, email 
        FROM user u 
        WHERE u.email = :email OR u.username = :username 
    """

interface UserRepository: CrudRepository<User, Long> {
    @Query(value=FIND_USER_QUERY, nativeQuery = true)
    fun findByUsername(@Param("username") username: String) : User?

    @Query(FIND_CONFLICT_QUERY, nativeQuery = true)
    fun findByUsernameOrEmail(@Param("username") username: String, @Param("email") email: String) : List<User>?
}
