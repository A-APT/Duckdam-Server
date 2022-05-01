package com.duckdam.controller

import com.duckdam.dto.auth.EmailTokenDto
import com.duckdam.service.EmailService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EmailController (private val emailService: EmailService) {

    @PostMapping("/user/email")
    fun generateEmailAuth(@RequestBody targetEmail: String): ResponseEntity<Unit> {
        emailService.sendEmailAuth(targetEmail)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/user/email/verify")
    fun verifyEmailToken(@RequestBody emailTokenDto: EmailTokenDto): ResponseEntity<Unit> {
        emailService.verifyEmailToken(emailTokenDto.email, emailTokenDto.token)
        return ResponseEntity.noContent().build()
    }
}
