package com.hrm.entity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="employee")
public class Employee extends CommonEntity{
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "employee_code")
    private String employeeCode;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "type_contract")
    private Integer typeContract;
    
    @Column(name = "picture_profile")
    private String pictureProfile;
    
    @Column(name = "delete_flag")
    private Integer deleteFlag;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "updated_by")
    private Integer updatedBy;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "paid_leave")
    private Float paidLeave = (float) 0;
    
    @Column(name = "compensatory_leave")
    private Float compensatoryLeave = (float) 0;
    
    @Column(name = "ot_unpaid")
    private Float otUnpaid = (float) 0;
    
    @Column(name="picture_name")
    private String pictureName;
    
    @Column(name="picture_type")
    private String pictureType;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "review_date")
    private Date reviewDate;
    
    @Column(name = "booking_day_off_notify")
    private Boolean bookingDayOffNotify;
    
    @Column(name = "confirm_day_off_notify")
    private Boolean confirmDayOffNotify;
    
    @Column(name = "booking_meeting_notify")
    private Boolean bookingMeetingNotify;
    
    @Column(name = "confirm_meeting_notify")
    private Boolean confirmMeetingNotify;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<BookingDayOff> bookingDayOff;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<DayOff> dayOff;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = true)
    @JsonProperty
    private Department department;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_group_id", nullable = true)
    @JsonProperty
    private RoleGroup roleGroup;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<Profile> profile;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<TimeKeeping> timeKeeping;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<DetailTimeKeeping> detailTimeKeeping;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<BookingRoom> bookingRoom;
    
    @Column(name = "reset_password_token")
    private String resetPasswordToken;
    
    @Column(name = "expire_time_token")
    private Timestamp  expireTimeToken;
    
    public void setCreateEmployeeEditByAndFlag(Integer idLogin,Integer flag) {
        this.createdBy = idLogin;
        this.updatedBy = idLogin;
        this.deleteFlag = flag;
    }
    
    public void setUpdateEmployeeEditByAndFlag(Integer idLogin,Integer flag) {
        this.updatedBy = idLogin;
        this.deleteFlag = flag;
    }
    
    public void setDeleteEmployeeEditAndFlag(Integer idLogin,Integer flag) {
        this.updatedBy = idLogin;
        this.deleteFlag = flag;
    }
}
