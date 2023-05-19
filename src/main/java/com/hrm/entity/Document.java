package com.hrm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hrm.common.CommonService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Document extends CommonEntity{
	@Column(name = "name")
    private String name;
    
    @Column(name = "content")
    private byte[] content;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "delete_flag")
    private Boolean deleteFlag;
    
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;
    
    @Column(name = "updated_by", nullable = false)
    private Integer updatedBy;

	@Override
	public void setCommonRegister() {
		super.setCommonRegister();
        this.createdBy = new CommonService().idUserAccountLogin();
        this.updatedBy = createdBy;
	}

	@Override
	public void setCommonUpdate() {
		// TODO Auto-generated method stub
		super.setCommonUpdate();
        this.updatedBy = new CommonService().idUserAccountLogin();
	}
}
