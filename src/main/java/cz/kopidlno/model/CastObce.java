package cz.kopidlno.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cast_obce")
@Data
@NoArgsConstructor
public class CastObce {

    @Id
    private Long kod;

    @Column(nullable = false)
    private String nazev;

    @Column(name = "obec_kod", nullable = false)
    private Long obecKod;
}
