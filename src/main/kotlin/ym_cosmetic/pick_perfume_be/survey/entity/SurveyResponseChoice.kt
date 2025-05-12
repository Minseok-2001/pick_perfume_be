package ym_cosmetic.pick_perfume_be.survey.entity

import jakarta.persistence.*

@Entity
@Table(name = "survey_response_choice")
class SurveyResponseChoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    val choiceId: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    val response: SurveyResponse,
    
    @Column(name = "option_text", nullable = false)
    val optionText: String
)
