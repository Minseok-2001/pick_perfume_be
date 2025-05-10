package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.accord.entity.Accord
import ym_cosmetic.pick_perfume_be.brand.entity.Brand
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.designer.entity.Designer
import ym_cosmetic.pick_perfume_be.member.entity.Member
import ym_cosmetic.pick_perfume_be.note.entity.Note
import ym_cosmetic.pick_perfume_be.perfume.enums.DesignerRole
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
    private var name: String,

    @Column(length = 5000)
    private var description: String? = null,

    @Column
    private var releaseYear: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "brand_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private var brand: Brand,

    @OneToMany(mappedBy = "perfume")
    private val designers: MutableList<PerfumeDesigner> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column
    private var concentration: Concentration? = null,

    @Embedded
    private var image: ImageUrl? = null,

    @Column(nullable = false)
    private var isApproved: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "creator_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private var creator: Member? = null,

    @OneToMany(mappedBy = "perfume")
    private val votes: MutableList<Vote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    private val reviews: MutableList<Review> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    private val perfumeNotes: MutableList<PerfumeNote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    private val perfumeAccords: MutableList<PerfumeAccord> = mutableListOf()

) : BaseTimeEntity() {

    companion object {
        fun create(
            name: String,
            brand: Brand,
            description: String? = null,
            releaseYear: Int? = null,
            concentration: Concentration? = null,
            image: ImageUrl? = null,
            creator: Member? = null,
            isAdmin: Boolean = false
        ): Perfume {
            require(name.isNotBlank()) { "향수 이름은 비어있을 수 없습니다." }
            
            return Perfume(
                name = name,
                brand = brand,
                description = description,
                releaseYear = releaseYear,
                concentration = concentration,
                image = image,
                creator = creator,
                isApproved = isAdmin
            )
        }
    }

    fun getName(): String = this.name
    
    fun getDescription(): String? = this.description
    
    fun getReleaseYear(): Int? = this.releaseYear
    
    fun getBrand(): Brand = this.brand
    
    fun getConcentration(): Concentration? = this.concentration
    
    fun getImage(): ImageUrl? = this.image
    
    fun isApproved(): Boolean = this.isApproved
    
    fun getCreator(): Member? = this.creator

    fun getNotes(): List<PerfumeNote> {
        return perfumeNotes.toList()
    }

    fun getNotesByType(type: NoteType): List<PerfumeNote> {
        return perfumeNotes.filter { it.getType() == type }
    }

    fun getAccords(): List<PerfumeAccord> {
        return perfumeAccords.toList()
    }

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
        return this
    }

    fun updateImage(image: ImageUrl?): Perfume {
        this.image = image
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
        return perfumeDesigner
    }

    fun removeDesigner(designer: Designer, role: DesignerRole) {
        designers.removeIf {
            it.getDesigner().id == designer.id && it.getRole() == role
        }
    }

    // 특정 역할의 디자이너들 조회
    fun getDesignersByRole(role: DesignerRole): List<Designer> {
        return designers
            .filter { it.getRole() == role }
            .map { it.getDesigner() }
    }

    fun getPrimaryPerfumer(): Designer? {
        return designers
            .find { it.getRole() == DesignerRole.PERFUMER }
            ?.getDesigner()
    }

    fun calculateAverageRating(): Double {
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.getRating().value }.average()
    }

    fun getReviewCount(): Int {
        return reviews.size
    }

    fun getVoteResults(): Map<VoteCategory, Map<String, Int>> {
        return votes.groupBy { it.getCategory() }
            .mapValues { (_, categoryVotes) ->
                categoryVotes.groupBy { it.getValue() }
                    .mapValues { it.value.size }
            }
    }

    fun getVoteResultByCategory(category: VoteCategory): Map<String, Int> {
        return votes.filter { it.getCategory() == category }
            .groupBy { it.getValue() }
            .mapValues { it.value.size }
    }

    fun getMostVotedValueByCategory(): Map<VoteCategory, String?> {
        return VoteCategory.entries.associateWith { category ->
            votes.filter { it.getCategory() == category }
                .groupBy { it.getValue() }
                .maxByOrNull { it.value.size }
                ?.key
        }
    }
}