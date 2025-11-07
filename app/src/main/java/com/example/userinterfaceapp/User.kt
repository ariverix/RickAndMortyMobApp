package com.example.userinterfaceapp

import java.io.Serializable

data class User(
    val name: String,
    val email: String,
    val password: String,
    val age: String,
    val gender: String
) : Serializable
