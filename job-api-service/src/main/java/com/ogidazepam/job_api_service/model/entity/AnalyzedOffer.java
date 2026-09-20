package com.ogidazepam.job_api_service.model.entity;

import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(
        name = "analyzed_offer",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_url",
                        columnNames = {"cvHash", "offer_url"}
                )
        },
        indexes = {
                @Index(name = "idx_analyzed_offer_analyzed_at", columnList = "analyzed_at DESC")
        }
)
public class AnalyzedOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String offerUrl;

    @Column(nullable = false)
    private String cvHash;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @CreationTimestamp
    private OffsetDateTime analyzedAt;
}
