package com.duckdam.service

import com.duckdam.domain.compliment.Compliment
import com.duckdam.domain.compliment.ComplimentRepository
import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.dto.compliment.ComplimentResponseDto
import com.duckdam.errors.exception.ForbiddenException
import com.duckdam.errors.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*
import kotlin.random.Random

@Service
class ComplimentService (
    private val complimentRepository: ComplimentRepository,
    private val userRepository: UserRepository,
) {
    fun generateCompliment(fromId: Long, complimentRequestDto: ComplimentRequestDto): Long {
        runCatching {
            userRepository.findById(complimentRequestDto.toId).get()
        }.onFailure {
            throw NotFoundException("User [${complimentRequestDto.toId}] was not registered.")
        }
        return complimentRepository.save(Compliment(
            stickerNum = complimentRequestDto.stickerNum,
            fromId = fromId,
            toId = complimentRequestDto.toId,
            message = complimentRequestDto.message,
            date = Date()
        )).id
    }

    fun findCompliments(toId: Long): ResponseEntity<List<ComplimentResponseDto>> {
        val complimentList: List<ComplimentResponseDto> = complimentRepository
            .findAllByToId(toId = toId)
            .map { it.toComplimentResponseDto() }
            .toList()
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                complimentList
            )
    }

    fun findComplimentsByFromAndTo(fromId: Long, toId: Long): ResponseEntity<List<ComplimentResponseDto>> {
        runCatching {
            userRepository.findById(toId).get()
        }.onFailure {
            throw NotFoundException("User [${toId}] was not registered.")
        }
        val complimentList: List<ComplimentResponseDto> = complimentRepository
            .findAllByFromIdAndToId(fromId = fromId, toId = toId)
            .map { it.toComplimentResponseDto() }
            .toList()
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                complimentList
            )
    }

    val compliments: List<String> = listOf(
        "자존감이 낮아져 있어도 괜찮아요. 그 덕에 더 노력할 수 있었고, 때론 무기력에 빠져 쉬는 시간도 가질 수 있었으니까요. 윤홍균, [자존감 수업]",
        "우리는 다르게 만들어졌어요. 이 사실을 받아들이면 다른 사람과 비교하거나 경쟁하는 일도 없을 겁니다. . -루이스 헤이, [치유]",
        "당신이 되고 싶었던 어떤 존재가 되기에는 지금도 결코 늦지 않았습니다. -조지 엘리엇",
        "오늘 정말 잘했어요. 실수하지 않아서가 아니라 포기하지 않아서. 뒤처지지 않아서가 아니라 멈춰 서지 않아서. -정영욱 [편지할게요]",
        "길을 모르면 물으면 될 것이고, 길을 잃으면 헤매면 그만입니다. 중요한 것은 나의 목적지가 어디인지 늘 잊지 않는 마음입니다. - 히피의 여행 바이러스",
        "탁월한 능력은 새로운 과제를 만날 때마다 스스로 발전하고 드러냅니다. -발타사르 그라시안",
        "어떤 일이 일어나든 확실한 것은 당신만의 잘못은 아닙니다. 지나치게 자책하지 않아도 괜찮아요.",
        "나는 세상에 하나뿐인 존재입니다.",
        "행복의 비결은 필요한 것을 얼마나 갖고 있는 가가 아니라, 불필요한 것에서 얼마나 자유로워져 있는가 하는 것입니다. -법정 스님 [행복의 비결]",
        "페이지를 넘겨요. 이미 지나간 일은 돌아보지 말고, 현재에 머물지도 말고, 페이지를 넘겨요.. -로빈 위 어러 [더 이상 우울한 월요일은 없다]",
        "오늘도 수고했어요! 바깥 하늘을 보면서 마음을 달래보아요 :)",
        "힘든 일이 많았겠지만, 그만큼 돌아올거에요!",
        "너무 자책하지 마세요. 그건 당신의 잘못이 아니에요.",
        "오늘도 멋진 하루를 보낸 당신! 맛있는 것으로 충전하면서 기분 좋게 하루를 마무리하세요 ;)",
        "모두가 같은 속도로 갈 수는 없어요. 늦었다고 생각하지 말고, 꾸준히 목표로 나아가요.",
        "당신이 웃기만 해도 세상이 다 환해지네요!",
        "어렵지만 열심히 노력해줘서 고마워요.",
        "너보다 잘하는 사람을 본 적이 없어. 정말 대단해!",
        "지금 당장은 어렵게 느껴져도, 너는 곧 대단한 일을 해낼 수 있어!",
        "늘 즐거운 일들을 만들어줘서 고마워. 너가 최고야!",
        "씨앗, 너무 애쓰지마. 너는 본디 꽃이 될 운명일지니  - 박광수(앗싸라비아)",
        "당신이 인생의 주인공이다.",
        "포기하지만 않으면 시간 차이일 뿐이야.",
        "너 오늘 좀 멋져보이는걸?",
        "걱정하지 마 넌 충분히 잘 하고 있어",
        "남들과 비교하지 말고 넌 너의 길을 가면 돼",
        "자신감 있는 표정을 지으면 자신감이 생긴다.",
        "내 비장의 무기는 아직 손 안에 있다. 그것은 희망이다. - 나폴레옹",
        "부정적인 생각을 내쫓고 긍정적인 생각으로 대체하자!",
        "좋은 꿈 꿔!",
    )
    fun slot(userId: Long): ResponseEntity<ComplimentResponseDto> {
        val user: User = userRepository.findById(userId).get()
        val latestSlot: LocalDate = user.latestSlot
        val today: LocalDate = LocalDate.now()
        val isEligibleForSlot: Boolean = today.isAfter(latestSlot)
        if (!isEligibleForSlot) {
            throw ForbiddenException("User has already drawn a compliment from the slot today.")
        }

        // get random sticker with fixed probability
        // (확률) 동글이들 각 15 : 한정판 각 5
        val r: Double = Random.nextDouble()
        val choice: Int = when (r) {
            in 0.00..0.15 -> 0
            in 0.15..0.30 -> 1
            in 0.30..0.45 -> 2
            in 0.45..0.60 -> 3
            in 0.60..0.75 -> 4
            in 0.75..0.80 -> 5
            in 0.80..0.85 -> 6
            in 0.85..0.90 -> 7
            in 0.90..0.95 -> 8
            in 0.95..1.00 -> 9
            else -> 0
        }
        val comment: String = compliments[Random.nextInt(compliments.size)]

        val compliment: Compliment = Compliment(
            stickerNum = choice,
            fromId = -1, // slot
            toId = userId,
            message = comment,
            date = Date()
        )

        // add to compliment table
        complimentRepository.save(compliment)

        // update user's latestSlot
        user.latestSlot = LocalDate.now()
        userRepository.save(user)

        return return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                compliment.toComplimentResponseDto()
            )
    }
}
