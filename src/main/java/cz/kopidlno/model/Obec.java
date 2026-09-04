package cz.kopidlno.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "obec")
@Data
@NoArgsConstructor
public class Obec {

    @Id
    private Long kod;

    @Column(nullable = false)
    private String nazev;
}
