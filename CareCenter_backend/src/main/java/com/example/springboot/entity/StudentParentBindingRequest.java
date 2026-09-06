package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("student_parent_binding_request")
public class StudentParentBindingRequest {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 家长用户名
     */
    private String parentUsername;

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 申请状态: pending(待审核), student_approved(学生已同意待管理审核), admin_approved(管理员已审核), 
     *          rejected(已拒绝), cancelled(已取消)
     */
    private String status;

    /**
     * 学生是否确认: 0-未确认, 1-已确认
     */
    private Integer studentConfirmed;

    /**
     * 学生确认时间
     */
    private LocalDateTime studentConfirmTime;

    /**
     * 管理员审核状态: 0-未审核, 1-已通过, 2-已拒绝
     */
    private Integer adminStatus;

    /**
     * 管理员审核时间
     */
    private LocalDateTime adminConfirmTime;

    /**
     * 管理员审核人
     */
    private String adminUsername;

    /**
     * 管理员拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParentUsername() {
        return parentUsername;
    }

    public void setParentUsername(String parentUsername) {
        this.parentUsername = parentUsername;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStudentConfirmed() {
        return studentConfirmed;
    }

    public void setStudentConfirmed(Integer studentConfirmed) {
        this.studentConfirmed = studentConfirmed;
    }

    public LocalDateTime getStudentConfirmTime() {
        return studentConfirmTime;
    }

    public void setStudentConfirmTime(LocalDateTime studentConfirmTime) {
        this.studentConfirmTime = studentConfirmTime;
    }

    public Integer getAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(Integer adminStatus) {
        this.adminStatus = adminStatus;
    }

    public LocalDateTime getAdminConfirmTime() {
        return adminConfirmTime;
    }

    public void setAdminConfirmTime(LocalDateTime adminConfirmTime) {
        this.adminConfirmTime = adminConfirmTime;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}