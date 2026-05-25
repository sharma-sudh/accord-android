package com.sudh.accord

import android.app.Application
import com.sudh.accord.auth.TokenManager
import com.sudh.accord.repository.AuthRepository
import com.sudh.accord.repository.TaskRepository
import com.sudh.accord.repository.UserRepository

class AccordApplication : Application() {

    lateinit var tokenManager: TokenManager
    lateinit var taskRepository: TaskRepository
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        taskRepository = TaskRepository()
        authRepository = AuthRepository()
        userRepository  = UserRepository()
    }
}