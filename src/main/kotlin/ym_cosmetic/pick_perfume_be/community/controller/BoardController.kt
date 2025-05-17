package ym_cosmetic.pick_perfume_be.community.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import ym_cosmetic.pick_perfume_be.common.dto.response.ApiResponse
import ym_cosmetic.pick_perfume_be.community.dto.request.BoardCreateRequest
import ym_cosmetic.pick_perfume_be.community.dto.request.BoardUpdateRequest
import ym_cosmetic.pick_perfume_be.community.dto.response.BoardResponse
import ym_cosmetic.pick_perfume_be.community.service.BoardService
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.member.enums.MemberRole
import ym_cosmetic.pick_perfume_be.security.CurrentMember
import ym_cosmetic.pick_perfume_be.security.RequireRole

@RestController
@RequestMapping("/api/boards")
@Tag(name = "게시판 API", description = "게시판 관련 API")
class BoardController(
    private val boardService: BoardService
) {

    @GetMapping
    @Operation(summary = "전체 게시판 조회", description = "모든 게시판 목록을 조회합니다.")
    fun getAllBoards(): ApiResponse<List<BoardResponse>> {
        val boards = boardService.getAllBoards()
        return ApiResponse.success(boards)
    }

    @GetMapping("/active")
    @Operation(summary = "활성화된 게시판 조회", description = "활성화된 게시판 목록을 조회합니다.")
    fun getActiveBoards(): ApiResponse<List<BoardResponse>> {
        val boards = boardService.getAllActiveBoards()
        return ApiResponse.success(boards)
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "게시판 상세 조회", description = "게시판 ID로 특정 게시판을 조회합니다.")
    fun getBoard(@PathVariable boardId: Long): ApiResponse<BoardResponse> {
        val board = boardService.getBoard(boardId)
        return ApiResponse.success(board)
    }

    @GetMapping("/by-name/{name}")
    @Operation(summary = "게시판 이름으로 조회", description = "게시판 이름으로 특정 게시판을 조회합니다.")
    fun getBoardByName(@PathVariable name: String): ApiResponse<BoardResponse> {
        val board = boardService.getBoardByName(name)
        return ApiResponse.success(board)
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다.")
    fun createBoard(
        @Valid @RequestBody request: BoardCreateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val boardId = boardService.createBoard(
            name = request.name,
            displayName = request.displayName,
            description = request.description,
            displayOrder = request.displayOrder
        )
        return ApiResponse.success(boardId)
    }

    @RequireRole(MemberRole.ADMIN)
    @PutMapping("/{boardId}")
    @Operation(summary = "게시판 수정", description = "게시판 정보를 수정합니다.")
    fun updateBoard(
        @PathVariable boardId: Long,
        @Valid @RequestBody request: BoardUpdateRequest,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val updatedBoardId = boardService.updateBoard(
            id = boardId,
            displayName = request.displayName,
            description = request.description,
            displayOrder = request.displayOrder
        )
        return ApiResponse.success(updatedBoardId)
    }

    @RequireRole(MemberRole.ADMIN)
    @PatchMapping("/{boardId}/activate")
    @Operation(summary = "게시판 활성화", description = "게시판을 활성화 상태로 변경합니다.")
    fun activateBoard(
        @PathVariable boardId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val activatedBoardId = boardService.activateBoard(boardId)
        return ApiResponse.success(activatedBoardId)
    }

    @RequireRole(MemberRole.ADMIN)
    @PatchMapping("/{boardId}/deactivate")
    @Operation(summary = "게시판 비활성화", description = "게시판을 비활성화 상태로 변경합니다.")
    fun deactivateBoard(
        @PathVariable boardId: Long,
        @CurrentMember member: Member
    ): ApiResponse<Long> {
        val deactivatedBoardId = boardService.deactivateBoard(boardId)
        return ApiResponse.success(deactivatedBoardId)
    }
} 