package com.duckdam.domain.compliment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ComplimentRepository: JpaRepository<Compliment, Long> {
}