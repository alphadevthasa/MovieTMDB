package com.thasa.moviedb.core.data

import com.thasa.moviedb.core.domain.AppError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun UnknownHostException_toNoNetwork() {
        assertEquals(AppError.NoNetwork, UnknownHostException().toAppError())
    }

    @Test
    fun SocketTimeoutException_toTimeout() {
        assertEquals(AppError.Timeout, SocketTimeoutException().toAppError())
    }

    @Test
    fun HttpException401_toUnauthorized() {
        assertEquals(AppError.Unauthorized, http(401).toAppError())
    }

    @Test
    fun HttpException404_toNotFound() {
        assertEquals(AppError.NotFound, http(404).toAppError())
    }

    @Test
    fun HttpException503_toServer503() {
        assertEquals(AppError.Server(503), http(503).toAppError())
    }

    @Test
    fun IllegalStateException_toUnknown() {
        assertTrue(IllegalStateException("x").toAppError() is AppError.Unknown)
    }

    @Test
    fun appError_passesThroughUnchanged() {
        assertEquals(AppError.NotFound, AppError.NotFound.toAppError())
    }
}
