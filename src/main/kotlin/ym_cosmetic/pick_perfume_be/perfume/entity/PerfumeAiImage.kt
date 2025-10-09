package ym_cosmetic.pick_perfume_be.perfume.entity

import jakarta.persistence.*
import ym_cosmetic.pick_perfume_be.common.BaseTimeEntity
import ym_cosmetic.pick_perfume_be.common.vo.ImageUrl
import ym_cosmetic.pick_perfume_be.perfume.enums.PerfumeAiImagePromptType

@Entity
@Table(
    name = "perfume_ai_image",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_perfume_ai_image_prompt",
            columnNames = ["perfume_id", "prompt_type"]
        )
    ]
)
class PerfumeAiImage private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id", nullable = false)
    val perfume: Perfume,

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false, length = 50)
    val promptType: PerfumeAiImagePromptType,

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    val prompt: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "url", column = Column(name = "image_url", length = 1024))
    )
    val imageUrl: ImageUrl
) : BaseTimeEntity() {

    companion object {
        fun create(
            perfume: Perfume,
            promptType: PerfumeAiImagePromptType,
            prompt: String,
            imageUrl: ImageUrl
        ): PerfumeAiImage {
            return PerfumeAiImage(
                perfume = perfume,
                promptType = promptType,
                prompt = prompt,
                imageUrl = imageUrl
            )
        }
    }
}
