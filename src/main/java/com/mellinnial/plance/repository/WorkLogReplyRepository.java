package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.WorkLogReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkLogReplyRepository extends JpaRepository<WorkLogReplyEntity, Long> {
}
