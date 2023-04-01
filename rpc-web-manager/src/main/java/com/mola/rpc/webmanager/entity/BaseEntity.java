package com.mola.rpc.webmanager.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-25 23:01
 **/

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Column(name = "gmt_create", length = 64)
    private Date gmtCreate;

    @Column(name = "gmt_modify", length = 64)
    private Date gmtModify;

    @Column(name = "creator", length = 64)
    private String creator;

    @Column(name = "modifier", length = 64)
    private String modifier;
}
