package com.nimbachi.franquicias.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "franchises")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Franchise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "franchise", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Branch> branches = new ArrayList<>();

    /**
     * Añade una nueva sucursal a la franquicia, asegurando la consistencia de la relación bidireccional.
     * @param branch La sucursal a añadir.
     */
    public void addBranch(Branch branch) {
        branches.add(branch);
        branch.setFranchise(this); // Mantiene la consistencia
    }

    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la franquicia no puede ser nulo o vacío.");
        }
        this.name = newName.trim();
    }

}
