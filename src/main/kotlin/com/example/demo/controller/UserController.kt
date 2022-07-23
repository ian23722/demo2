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
            /**
             * 새 유저 등록시에는 username과 email이 이미 등록되어 있는것인지 먼저 확인한 후에 등록
             */
    fun signup(@RequestParam("username") username: String,
               @RequestParam("password") password: String,
               @RequestParam("email") email: String): SignupResponse {
        // sign up 하려는 username 이나 email 로 등록 되어 있는 유저 목록을 가져온다
        val existingUsers : List<User> = repository.findByUsernameOrEmail(username, email) ?: listOf()
        // 중복된 username이나 email이 있을 경우 새 유저를 등록 시키지 않고 에러 메세지를 반환
        if (existingUsers.isNotEmpty()) {
            val existingUser = existingUsers[0]
            if (existingUser.username == username)
                return SignupResponse("username_already_exists")
            if (existingUser.email == email)
                return SignupResponse("email_already_exists")
        }

        // 패스워드는 직접적으로 저장하지 않고 password hash를 저장한다... 이 방법으로 패스워드의 직접적인 유출을 막을수 있다.
        val hashedPassword = passwordEncoder.encode(password)
        val user = User(null, username, hashedPassword, Role.USER, email)
        return try {
            // 새로운 유저를 데이터베이스에 저장하여 유저 등록을 마친다..
            repository.save(user)
            SignupResponse("signup_succeeded")
        } catch (ex: java.lang.RuntimeException) {
            // 에러가 발생할 경우 (ex. 데이터 베이스 에러) 에러 메세지를 반환한다....
            // 일반적인 경우 로그 메세지를 남겨 troubleshooting을 가능하게 만들어야 한다..
            SignupResponse("signup_failed")
        }
    }

    data class SignupResponse(val sign_status: String)
}
