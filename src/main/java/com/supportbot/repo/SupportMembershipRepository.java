package com.supportbot.repo;

import com.supportbot.domain.SupportMembership;
import com.supportbot.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportMembershipRepository extends JpaRepository<SupportMembership, Long> {
    Optional<SupportMembership> findByUserProfileIdAndAdminGroupId(Long userProfileId, Long adminGroupId);
    List<SupportMembership> findTop10ByUserProfileOrderByLastUsedAtDesc(UserProfile userProfile);
}