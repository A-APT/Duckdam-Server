package com.duckdam.domain.friend

import javax.persistence.*

@Entity
class Friend (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,

    /* uid is friend of friendId, not vice versa */

    @Column(nullable = false)
    var uid: Long, // my

    @Column(nullable = false)
    var friendId: Long, // target
)
