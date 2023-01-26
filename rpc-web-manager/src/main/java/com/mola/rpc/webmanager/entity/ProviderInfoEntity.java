package com.mola.rpc.webmanager.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-01-24 15:15
 **/
@Entity
@Table(name = "tb_provider_info")
@Data
public class ProviderInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interface_clazz_name", nullable = false, length = 256)
    private String interfaceClazzName;

    @Column(name = "provider_group", nullable = false, length = 32)
    private String providerGroup;

    @Column(name = "provider_version", nullable = false, length = 32)
    private String providerVersion;

    @Column(name = "environment", nullable = false, length = 32)
    private String providerEnvironment;

    @Column(name = "app_name", nullable = false, length = 32)
    private String providerAppName;

    @Column(name = "in_fiber", nullable = false, length = 8)
    private Boolean inFiber;

    @Column(name = "provider_bean_name", length = 64)
    private String providerBeanName;

    @Column(name = "provider_bean_clazz", length = 256)
    private String providerBeanClazz;

    @Column(name = "online", nullable = false, length = 8)
    private Boolean online;
}
