package com.sudh.accord

import android.app.Application
import com.sudh.accord.auth.TokenManager
import com.sudh.accord.network.RetrofitClient
import com.sudh.accord.repository.AnalyticsRepository
import com.sudh.accord.repository.AuthRepository
import com.sudh.accord.repository.PaymentRepository
import com.sudh.accord.repository.TaskRepository
import com.sudh.accord.repository.UserRepository

class AccordApplication : Application() {

    lateinit var tokenManager: TokenManager
    lateinit var taskRepository: TaskRepository
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var analyticsRepository: AnalyticsRepository
    lateinit var paymentRepository: PaymentRepository

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager) // must run before any repository touches RetrofitClient.api
        taskRepository = TaskRepository()
        authRepository = AuthRepository()
        userRepository  = UserRepository()
        analyticsRepository = AnalyticsRepository()
        paymentRepository = PaymentRepository()
    }
}