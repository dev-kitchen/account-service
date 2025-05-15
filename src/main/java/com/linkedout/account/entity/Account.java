package com.linkedout.account.entity;

import com.linkedout.common.model.type.RoleEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
  @Id
  @Column("id")
  private Long id;

  @Column("email")
  private String email;

  @Column("name")
  private String name;

  @Column("picture")
  private String picture;

  @Column("provider_id")
  private String providerId;

  @Column("provider")
  private String provider;

  @Column("role")
  @Builder.Default
  private RoleEnum role = RoleEnum.ROLE_USER;

  @Column("youtube")
  private String youtube;

  @Column("blog")
  private String blog;
}
