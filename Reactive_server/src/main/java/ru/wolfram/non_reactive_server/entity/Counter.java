package ru.wolfram.non_reactive_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "counter")
public class Counter {
    @Id
    @Column("id")
    private Long id;
    @Column("name")
    private String name;
    @Column("value")
    private Long value;
}
