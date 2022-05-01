package com.duckdam.domain.user

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors
import javax.persistence.*

@Document(collection="user")
class User (
    @Id
    var id: ObjectId = ObjectId(),
    var name: String,
    var password: String,
    var email: String,
    var roles: List<String>

) {
    fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.stream()
            .map { role: String? ->
                SimpleGrantedAuthority(
                    role
                )
            }
            .collect(Collectors.toList())
    }
}
