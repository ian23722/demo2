package com.example.demo.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(
    @Id
    @GeneratedValue
    var user_id: Long? = null,
    @Column(unique = true)
    var username: String? = null,
    /* password should never be stored as a plain text. Use SHA1 for now. */
    var password: String? = null,
    @Enumerated(EnumType.STRING)
    var role: Role? = null,
    @Column(unique = true)
    var email: String? = null,
    /* additional information can be added below */
)
