package com.masterbit.omdb.data

sealed class Data {
    object Loading: Data()
    data class Success<T>(val data: List<T>): Data()
    data class Error(val msg: String): Data()
}