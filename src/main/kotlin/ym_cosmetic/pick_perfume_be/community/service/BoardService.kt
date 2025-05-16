package ym_cosmetic.pick_perfume_be.community.service

import ym_cosmetic.pick_perfume_be.community.dto.response.BoardResponse

interface BoardService {
    
    fun createBoard(name: String, displayName: String, description: String?, displayOrder: Int): Long
    
    fun updateBoard(id: Long, displayName: String, description: String?, displayOrder: Int): Long
    
    fun activateBoard(id: Long): Long
    
    fun deactivateBoard(id: Long): Long
    
    fun getBoard(id: Long): BoardResponse
    
    fun getBoardByName(name: String): BoardResponse
    
    fun getAllBoards(): List<BoardResponse>
    
    fun getAllActiveBoards(): List<BoardResponse>
} 