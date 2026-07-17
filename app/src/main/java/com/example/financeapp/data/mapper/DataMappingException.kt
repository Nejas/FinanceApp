package com.example.financeapp.data.mapper

class DataMappingException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
