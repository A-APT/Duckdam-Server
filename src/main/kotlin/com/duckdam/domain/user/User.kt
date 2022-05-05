package com.duckdam.domain.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors
import javax.persistence.*

@Entity
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = true)
    var profile: String?, // byte64 encoded byte array

    @Column(nullable = false, length = 5)
    var sticker: String, // 00000 ~ 11111

    @ElementCollection(fetch = FetchType.EAGER)
    var roles: Set<String>

): UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.stream()
            .map { role: String? ->
                SimpleGrantedAuthority(
                    role
                )
            }
            .collect(Collectors.toList())
    }
    override fun getUsername(): String = email
    override fun getPassword(): String = password
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
