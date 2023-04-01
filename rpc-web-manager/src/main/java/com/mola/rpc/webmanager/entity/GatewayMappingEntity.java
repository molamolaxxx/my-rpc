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
@Table(name = "tb_gateway_mapping")
@Data
public class GatewayMappingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "description", nullable = false, length = 256)
    private String description;

    @Column(name = "request_mapping", nullable = false, length = 128)
    private String requestMapping;

    @Column(name = "environment", nullable = false, length = 256)
    private String environment;

    @Column(name = "request_handler", nullable = false, length = 64)
    private String requestHandler;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "interface_clazz_name", nullable = false, length = 256)
    private String interfaceClazzName;

    @Column(name = "provider_group", nullable = false, length = 32)
    private String providerGroup;

    @Column(name = "provider_version", nullable = false, length = 32)
    private String providerVersion;

    @Column(name = "load_balance_strategy", nullable = false, length = 32)
    private String loadBalanceStrategy;

    @Column(name = "timeout", nullable = false, length = 32)
    private Long timeout;

    @Column(name = "reverse_mode", nullable = false, length = 32)
    private Boolean reverseMode;

    @Column(name = "appointed_address", nullable = false, length = 32)
    private Boolean appointedAddress;

}
