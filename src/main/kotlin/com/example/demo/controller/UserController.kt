package com.example.demo.controller

import com.example.demo.dao.UserRepository
import com.example.demo.domain.Role
import com.example.demo.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Suppress("unused")
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
    fun login(@RequestParam("username") username: String, @RequestParam("password") password: String): LoginResponse {
        val user: User = repository.findByUsername(username) ?: return LoginResponse("failed")
        return if (passwordEncoder.matches(password, user.password)) {
            LoginResponse("succeeded")
        } else {
            LoginResponse("failed")
        }
    }

    data class LoginResponse(val login_status: String)

    @PostMapping(path = ["/signup"])
    @ResponseBody
    fun signup(@RequestParam("username") username: String,
               @RequestParam("password") password: String,
               @RequestParam("email") email: String): SignupResponse {
        val existingUsers : List<User> = repository.findByUsernameOrEmail(username, email) ?: listOf()
        if (existingUsers.isNotEmpty()) {
            val existingUser = existingUsers[0]
            if (existingUser.username == username)
                return SignupResponse("username_already_exists")
            if (existingUser.email == email)
                return SignupResponse("email_already_exists")
        }

        val hashedPassword = passwordEncoder.encode(password)
        val user = User(null, username, hashedPassword, Role.USER, email)
        return try {
            repository.save(user)
            SignupResponse("signup_succeeded")
        } catch (ex: java.lang.RuntimeException) {
            SignupResponse("signup_failed")
        }
    }

    data class SignupResponse(val sign_status: String)
}
