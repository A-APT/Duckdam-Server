package com.duckdam.domain.auth

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.*

@Document(collection="EmailAuth")
class EmailAuth (
    @Id
    var id: ObjectId = ObjectId(),
    var email: String, // target email
    var token: String, // validate token
    var expirationTime: Long, // time in milliseconds
    var expired: Boolean,
)
