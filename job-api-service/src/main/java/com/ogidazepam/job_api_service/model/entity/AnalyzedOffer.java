package com.ogidazepam.job_api_service.model.entity;

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
                        columnNames = {"customer_id", "offer_url"}
                )
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
    private String jobTitle;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 500)
    private String reason;

    @CreationTimestamp
    private OffsetDateTime analyzedAt;
}
