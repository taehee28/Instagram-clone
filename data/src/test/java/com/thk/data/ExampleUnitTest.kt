package com.thk.data

import com.thk.data.repository.LoginRepository
import com.thk.data.repository.LoginRepositoryImpl
import com.thk.data.repository.MainRepository
import com.thk.data.repository.MainRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private lateinit var loginRepository: LoginRepository

    @Before
    fun ready() {
        loginRepository = LoginRepositoryImpl()
    }

    @Test
    fun test_loginWithEmail() = runBlocking {
        loginRepository.signIn(
            email = "test@test.com",
            password = "123456",
            onStart = {},
            onSuccess = {},
            onError = { assert(false) { "$it" } }
        )
    }
}