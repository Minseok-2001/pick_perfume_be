package ym_cosmetic.pick_perfume_be.community.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ym_cosmetic.pick_perfume_be.common.exception.EntityNotFoundException
import ym_cosmetic.pick_perfume_be.community.dto.response.BoardResponse
import ym_cosmetic.pick_perfume_be.community.entity.Board
import ym_cosmetic.pick_perfume_be.community.repository.BoardRepository

@Service
@Transactional
class BoardService(
    private val boardRepository: BoardRepository
)  {

     fun createBoard(name: String, displayName: String, description: String?, displayOrder: Int): Long {
        val board = Board.create(name, displayName, description, displayOrder)
        return boardRepository.save(board).id
    }

     fun updateBoard(id: Long, displayName: String, description: String?, displayOrder: Int): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.update(displayName, description, displayOrder)
        
        return board.id
    }

     fun activateBoard(id: Long): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.activate()
        
        return board.id
    }

     fun deactivateBoard(id: Long): Long {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        board.deactivate()
        
        return board.id
    }

    @Transactional(readOnly = true)
     fun getBoard(id: Long): BoardResponse {
        val board = boardRepository.findById(id)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        return BoardResponse.from(board)
    }

    @Transactional(readOnly = true)
     fun getBoardByName(name: String): BoardResponse {
        val board = boardRepository.findByName(name)
            .orElseThrow { EntityNotFoundException("게시판을 찾을 수 없습니다.") }
        
        return BoardResponse.from(board)
    }

    @Transactional(readOnly = true)
     fun getAllBoards(): List<BoardResponse> {
        return boardRepository.findAll().map { BoardResponse.from(it) }
    }

    @Transactional(readOnly = true)
     fun getAllActiveBoards(): List<BoardResponse> {
        return boardRepository.findAllActiveOrderByDisplayOrder().map { BoardResponse.from(it) }
    }
} 