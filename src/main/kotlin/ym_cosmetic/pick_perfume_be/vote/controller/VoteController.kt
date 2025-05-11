package ym_cosmetic.pick_perfume_be.vote.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.common.exception.InvalidRequestException
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.vote.dto.request.VoteCreateRequest
import ym_cosmetic.pick_perfume_be.vote.dto.request.VoteUpdateRequest
import ym_cosmetic.pick_perfume_be.vote.dto.response.VoteCategoryStatistics
import ym_cosmetic.pick_perfume_be.vote.dto.response.VoteResponse
import ym_cosmetic.pick_perfume_be.vote.dto.response.VoteStatisticsResponse
import ym_cosmetic.pick_perfume_be.vote.service.VoteService
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory

@RestController
@RequestMapping("/api/votes")
class VoteController(
    private val voteService: VoteService
) {
    @GetMapping("/perfumes/{perfumeId}/statistics")
    fun getVoteStatistics(@PathVariable perfumeId: Long): ApiResponse<VoteStatisticsResponse> {
        val statistics = voteService.getVoteStatistics(perfumeId)

        val response = VoteStatisticsResponse(
            categories = statistics.map { (category, result) ->
                VoteCategoryStatistics.from(category, result)
            }
        )

        return ApiResponse.success(response)
    }

    @GetMapping("/perfumes/{perfumeId}/my-votes")
    fun getMyVotes(
        @PathVariable perfumeId: Long,
        @CurrentMember member: Member
    ): ApiResponse<List<VoteResponse>> {
        val votes = voteService.getVotesByMemberAndPerfume(member.id!!, perfumeId)

        return ApiResponse.success(votes.map { VoteResponse.from(it) })
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createVote(
        @Valid @RequestBody request: VoteCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<VoteResponse> {
        if (!request.isValidVote()) {
            throw InvalidRequestException(
                "Invalid vote value '${request.value}' for category ${request.category}. " +
                        "Allowed values: ${request.category.getAllowedValues()}"
            )
        }

        val vote = voteService.createVote(
            memberId = member.id!!,
            perfumeId = request.perfumeId,
            category = request.category,
            value = request.value
        )

        return ApiResponse.success(VoteResponse.from(vote))
    }

    @PutMapping("/{id}")
    fun updateVote(
        @PathVariable id: Long,
        @RequestBody request: VoteUpdateRequest,
        @CurrentMember member: Member
    ): ApiResponse<VoteResponse> {
        val vote = voteService.updateVote(id, request.value)
        return ApiResponse.success(VoteResponse.from(vote))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVote(
        @PathVariable id: Long,
        @CurrentMember member: Member
    ) {
        voteService.deleteVote(id)
    }


    @GetMapping("/perfumes/{perfumeId}/categories/{category}")
    fun getVotesByCategory(
        @PathVariable perfumeId: Long,
        @PathVariable category: VoteCategory
    ): ApiResponse<Map<String, Int>> {
        val voteCounts = voteService.getVoteCountsByCategory(perfumeId, category)
        return ApiResponse.success(voteCounts)
    }
}