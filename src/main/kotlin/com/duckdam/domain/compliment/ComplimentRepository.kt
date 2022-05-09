package com.duckdam.domain.compliment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ComplimentRepository: JpaRepository<Compliment, Long> {
    fun findAllByFromId(fromId: Long): MutableList<Compliment>
    fun findAllByToId(toId: Long): MutableList<Compliment>
    fun findAllByFromIdAndToId(fromId: Long, toId: Long): MutableList<Compliment>
}