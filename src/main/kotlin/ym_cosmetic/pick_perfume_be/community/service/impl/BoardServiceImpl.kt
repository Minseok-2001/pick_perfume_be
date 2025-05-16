package ym_cosmetic.pick_perfume_be.community.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.community.dto.response.BoardResponse
import ym_cosmetic.pick_perfume_be.community.entity.Board
import ym_cosmetic.pick_perfume_be.community.repository.BoardRepository
import ym_cosmetic.pick_perfume_be.community.service.BoardService

@Service
@Transactional
class BoardServiceImpl(
    private val boardRepository: BoardRepository
) : BoardService {

    override fun createBoard(name: String, displayName: String, description: String?, displayOrder: Int): Long {
        val board = Board.create(name, displayName, description, displayOrder)
        return boardRepository.save(board).id
    }

    override fun updateBoard(id: Long, displayName: String, description: String?, displayOrder: Int): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.update(displayName, description, displayOrder)
        
        return board.id
    }

    override fun activateBoard(id: Long): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.activate()
        
        return board.id
    }

    override fun deactivateBoard(id: Long): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.deactivate()
        
        return board.id
    }

    @Transactional(readOnly = true)
    override fun getBoard(id: Long): BoardResponse {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        return BoardResponse.from(board)
    }

    @Transactional(readOnly = true)
    override fun getBoardByName(name: String): BoardResponse {
        val board = boardRepository.findByName(name)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        return BoardResponse.from(board)
    }

    @Transactional(readOnly = true)
    override fun getAllBoards(): List<BoardResponse> {
        return boardRepository.findAll().map { BoardResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getAllActiveBoards(): List<BoardResponse> {
        return boardRepository.findAllActiveOrderByDisplayOrder().map { BoardResponse.from(it) }
    }
} 