package com.runningwith.domain.event;

import com.runningwith.domain.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@NamedEntityGraph(
        name = "Enrollment.withEventAndStudy",
        attributeNodes = {
                @NamedAttributeNode(value = "eventEntity", subgraph = "studyEntity")
        },
        subgraphs = @NamedSubgraph(name = "studyEntity", attributeNodes = @NamedAttributeNode("studyEntity"))
)
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enrollment", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_event")
    private EventEntity eventEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_users")
    private UsersEntity usersEntity;

    @Column(nullable = false, name = "enrolled_at")
    private LocalDateTime enrolledAt;

    private boolean accepted = false;

    private boolean attended = false;

    public EnrollmentEntity(UsersEntity usersEntity, LocalDateTime enrolledAt, boolean accepted) {
        this.usersEntity = usersEntity;
        this.enrolledAt = enrolledAt;
        this.accepted = accepted;
    }

    public void setEvent(EventEntity eventEntity) {
        this.eventEntity = eventEntity;
    }

    public void updateAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void updateAttended(boolean attended) {
        this.attended = attended;
    }
}