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
class Perfume(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(length = 5000)
    var description: String? = null,

    @Column
    var releaseYear: Int? = null,

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
    var isApproved: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "creator_id", foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var creator: Member? = null,

    @OneToMany(mappedBy = "perfume")
    val votes: MutableList<Vote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val reviews: MutableList<Review> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val perfumeNotes: MutableList<PerfumeNote> = mutableListOf(),

    @OneToMany(mappedBy = "perfume")
    val perfumeAccords: MutableList<PerfumeAccord> = mutableListOf()

) : BaseTimeEntity() {
    fun getNotes(): List<PerfumeNote> {
        return perfumeNotes.toList()
    }

    fun getNotesByType(type: NoteType): List<PerfumeNote> {
        return perfumeNotes.filter { it.type == type }
    }

    fun getAccords(): List<PerfumeAccord> {
        return perfumeAccords.toList()
    }

    fun addNote(note: Note, type: NoteType) {
        val perfumeNote = PerfumeNote(
            perfume = this,
            note = note,
            type = type
        )
        perfumeNotes.add(perfumeNote)
    }

    fun addAccord(accord: Accord) {
        val perfumeAccord = PerfumeAccord(
            perfume = this,
            accord = accord
        )
        perfumeAccords.add(perfumeAccord)
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
    ) {
        this.name = name
        this.brand = brand
        this.description = description
        this.releaseYear = releaseYear
        this.concentration = concentration
    }

    fun updateImage(image: ImageUrl?) {
        this.image = image
    }


    fun addDesigner(designer: Designer, role: DesignerRole, description: String? = null) {
        val perfumeDesigner = PerfumeDesigner(
            perfume = this,
            designer = designer,
            role = role,
            description = description
        )
        designers.add(perfumeDesigner)
    }

    fun removeDesigner(designer: Designer, role: DesignerRole) {
        designers.removeIf {
            it.designer.id == designer.id && it.role == role
        }
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

    fun getReviewCount(): Int {
        return reviews.size
    }


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