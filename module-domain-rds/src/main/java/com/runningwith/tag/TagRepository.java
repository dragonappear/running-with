package com.runningwith.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    TagEntity findByTitle(String title);
}
