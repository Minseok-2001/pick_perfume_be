package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.brand.entity.Brand
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole
import ym_cosmetic.pick_perfume_be.perfume.enums.Gender
import ym_cosmetic.pick_perfume_be.perfume.vo.Concentration
import ym_cosmetic.pick_perfume_be.perfume.vo.NoteType
import ym_cosmetic.pick_perfume_be.review.entity.Review
import ym_cosmetic.pick_perfume_be.vote.entity.Vote
import ym_cosmetic.pick_perfume_be.vote.vo.VoteCategory

@Entity
@Table(name = "perfume")
class Perfume private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(length = 5000)
    var description: String? = null,

    @Column
    var releaseYear: Int? = null,

    @Column
    @Enumerated(EnumType.STRING)
    var gender: Gender,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "brand_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var brand: Brand,

    @OneToMany(mappedBy = "perfume")
    val designers: MutableList<PerfumeDesigner> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column
    var concentration: Concentration? = null,

    @Embedded
    var image: ImageUrl? = null,

    @Column(nullable = false)
    @ColumnDefault("false")
    var isApproved: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "creator_id", 
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    val creator: Member? = null,

    @OneToMany(mappedBy = "perfume")
    val votes: MutableList<Vote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val reviews: MutableList<Review> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val perfumeNotes: MutableList<PerfumeNote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val perfumeAccords: MutableList<PerfumeAccord> = mutableListOf(),

    @Column(nullable = false)
    var searchSynced: Boolean = false

) : BaseTimeEntity() {

    companion object {
        fun create(
            name: String,
            brand: Brand,
            gender: Gender,
            description: String? = null,
            releaseYear: Int? = null,
            concentration: Concentration? = null,
            image: ImageUrl? = null,
            creator: Member? = null,
            isAdmin: Boolean = false,
            searchSynced: Boolean = false
        ): Perfume {
            require(name.isNotBlank()) { "향수 이름은 비어있을 수 없습니다." }
            
            return Perfume(
                name = name,
                brand = brand,
                gender = gender,
                description = description,
                releaseYear = releaseYear,
                concentration = concentration,
                image = image,
                creator = creator,
                isApproved = isAdmin,
                searchSynced = searchSynced
            )
        }
    }

    fun getNotes(): List<PerfumeNote> = perfumeNotes.toList()

    fun getNotesByType(type: NoteType): List<PerfumeNote> = 
        perfumeNotes.filter { it.type == type }

    fun getAccords(): List<PerfumeAccord> = perfumeAccords.toList()

    fun addNote(note: Note, type: NoteType): PerfumeNote {
        val perfumeNote = PerfumeNote.create(
            perfume = this,
            note = note,
            type = type
        )
        perfumeNotes.add(perfumeNote)
        return perfumeNote
    }

    fun addAccord(accord: Accord): PerfumeAccord {
        val perfumeAccord = PerfumeAccord.create(
            perfume = this,
            accord = accord
        )
        perfumeAccords.add(perfumeAccord)
        return perfumeAccord
    }

    fun approve() {
        if (!isApproved) {
            isApproved = true
        }
    }

    fun updateDetails(
        name: String,
        brand: Brand,
        description: String?,
        releaseYear: Int?,
        concentration: Concentration?
    ): Perfume {
        require(name.isNotBlank()) { "향수 이름은 비어있을 수 없습니다." }
        
        this.name = name
        this.brand = brand
        this.description = description
        this.releaseYear = releaseYear
        this.concentration = concentration
        this.searchSynced = false // 데이터가 변경되었으므로 검색 동기화 상태를 false로 변경
        return this
    }

    fun updateImage(image: ImageUrl?): Perfume {
        this.image = image
        this.searchSynced = false
        return this
    }

    fun setSearchSynced(synced: Boolean): Perfume {
        this.searchSynced = synced
        return this
    }

    fun addDesigner(designer: Designer, role: DesignerRole, description: String? = null): PerfumeDesigner {
        val perfumeDesigner = PerfumeDesigner.create(
            perfume = this,
            designer = designer,
            role = role,
            description = description
        )
        designers.add(perfumeDesigner)
        this.searchSynced = false
        return perfumeDesigner
    }

    fun removeDesigner(designer: Designer, role: DesignerRole) {
        designers.removeIf {
            it.designer.id == designer.id && it.role == role
        }
        this.searchSynced = false
    }

    // 특정 역할의 디자이너들 조회
    fun getDesignersByRole(role: DesignerRole): List<Designer> {
        return designers
            .filter { it.role == role }
            .map { it.designer }
    }

    fun getPrimaryPerfumer(): Designer? {
        return designers
            .find { it.role == DesignerRole.PERFUMER }
            ?.designer
    }

    fun calculateAverageRating(): Double {
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.rating.value }.average()
    }

    fun getReviewCount(): Int = reviews.size

    fun getVoteResults(): Map<VoteCategory, Map<String, Int>> {
        return votes.groupBy { it.category }
            .mapValues { (_, categoryVotes) ->
                categoryVotes.groupBy { it.value }
                    .mapValues { it.value.size }
            }
    }

    fun getVoteResultByCategory(category: VoteCategory): Map<String, Int> {
        return votes.filter { it.category == category }
            .groupBy { it.value }
            .mapValues { it.value.size }
    }

    fun getMostVotedValueByCategory(): Map<VoteCategory, String?> {
        return VoteCategory.entries.associateWith { category ->
            votes.filter { it.category == category }
                .groupBy { it.value }
                .maxByOrNull { it.value.size }
                ?.key
        }
    }
}