package com.hrm.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.hrm.common.CommonService;
import com.hrm.utils.Utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
abstract class CommonEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Integer id;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP default NOW()")
    private Timestamp createdAt = Utils.getTimestamp();

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP default NOW()")
    private Timestamp updatedAt = Utils.getTimestamp();
    

    public void setCommonRegister() {
        this.createdAt = Utils.getTimestamp();
        this.updatedAt = Utils.getTimestamp();
    }

    public void setCommonUpdate() {
        this.updatedAt = Utils.getTimestamp();
    }

    public void setCommonDelete() {
        setCommonUpdate();
    }
}
