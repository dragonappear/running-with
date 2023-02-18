package com.runningwith.study;

import com.runningwith.tag.TagEntity;
import com.runningwith.users.UsersEntity;
import com.runningwith.zone.ZoneEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "study")
public class StudyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_study", nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(name = "study_managers", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_users"))
    private Set<UsersEntity> managers;

    @ManyToMany
    @JoinTable(name = "study_members", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_users"))
    private Set<UsersEntity> members;

    @ManyToMany
    @JoinTable(name = "study_tags", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<TagEntity> tags;

    @ManyToMany
    @JoinTable(name = "study_zones", joinColumns = @JoinColumn(name = "id_study"), inverseJoinColumns = @JoinColumn(name = "id_zone"))
    private Set<ZoneEntity> zones;

    @Column(unique = true)
    private String path;

    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "full_description")
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "banner_image")
    private String bannerImage;

    @Column(name = "published_datetime")
    private LocalDateTime publishedDatetime;

    @Column(name = "closed_datetime")
    private LocalDateTime closedDateTime;

    @Column(name = "recruiting_updated_datetime")
    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting = true;

    private boolean published = false;

    private boolean closed = false;

    @Column(name = "use_banner")
    private boolean useBanner = false;
}
