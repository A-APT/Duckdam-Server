package com.duckdam.domain.auth

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailAuthRepository: MongoRepository<EmailAuth, ObjectId> {
    fun findByEmail(email: String): EmailAuth
}