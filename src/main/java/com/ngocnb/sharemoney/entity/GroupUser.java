package com.ngocnb.sharemoney.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "group_users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(GroupUser.GroupUserId.class)
public class GroupUser {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "group_id")
    private Long groupId;

    private Date joinAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupUserId implements Serializable {
        private Long userId;
        private Long groupId;
    }
}
