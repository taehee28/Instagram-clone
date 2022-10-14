package com.thk.data

import com.thk.data.repository.LoginRepository
import com.thk.data.repository.LoginRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private lateinit var repository: LoginRepository

    @Before
    fun ready() {
        repository = LoginRepositoryImpl()
    }

    @Test
    fun test_loginWithEmail() = runBlocking {
        repository.signIn(
            email = "test@test.com",
            password = "123456",
            onStart = {},
            onSuccess = {},
            onError = { assert(false) { "$it" } }
        )
    }
}