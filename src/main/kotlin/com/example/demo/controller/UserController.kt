package com.example.demo.controller

import com.example.demo.dao.UserRepository
import com.example.demo.domain.Role
import com.example.demo.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping(path=["/user"])
class UserController {
    @Autowired
    private lateinit var repository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    //TODO move to post mapping - login information cannot be a part of url because it creates security vulnerability.
    @PostMapping(path = ["/login"])
    @ResponseBody
    fun login(@RequestParam("username") username: String, @RequestParam("password") password: String): String {
        val user: User = repository.findByUsername(username) ?: return """
            {"login":"failed"}
        """.trimIndent()
        return if (passwordEncoder.matches(password, user.password)) {
            """
            {"login":"succeeded"}
            """.trimIndent()
        } else {
            """
            {"login":"failed"}
            """.trimIndent()
        }
    }

    @PostMapping(path = ["/signup"])
    @ResponseBody
    fun signup(@RequestParam("username") username: String,
               @RequestParam("password") password: String,
               @RequestParam("email") email: String): String {
        val existingUsers : List<User> = repository.findByUsernameOrEmail(username, email) ?: listOf()
        if (existingUsers.isNotEmpty()) {
            val existingUser = existingUsers[0]
            if (existingUser.username == username)
                return "username already exists."
            if (existingUser.email == email)
                return "email already exists."
        }

        val hashedPassword = passwordEncoder.encode(password)
        val user = User(null, username, hashedPassword, Role.USER, email)
        return try {
            repository.save(user)
            "sign up succeeded."
        } catch (ex: java.lang.RuntimeException) {
            "sign up failed."
        }
    }
}
