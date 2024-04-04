package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="city")

public class CityEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   @ManyToOne
   //@JoinColumn(name="department_id",referencedColumnName = "id")
    private DepartmentEntity department;
    private String name;
}
