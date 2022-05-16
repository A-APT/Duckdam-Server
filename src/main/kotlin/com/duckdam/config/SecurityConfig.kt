package com.duckdam.config

import com.duckdam.security.JWTTokenProvider
import com.duckdam.security.JwtAuthenticationFilter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
class SecurityConfig(private val jwtTokenProvider: JWTTokenProvider): WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .headers().frameOptions().disable()
            .and()
                .authorizeRequests()
                .antMatchers("/friend/**",
                                        "/compliment/**",
                                        "/compliments/**",
                                        "/user/stickers",
                                        "/user/slot",
                                        "/users/**").hasRole("USER")
                .antMatchers("/**").permitAll()
            .and()
                .addFilterBefore(
                    JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter::class.java
                )
    }
}
