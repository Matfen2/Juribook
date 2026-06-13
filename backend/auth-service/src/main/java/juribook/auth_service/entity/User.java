package juribook.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // (pour la table V1_create_user_table.sql)
@Getter @Setter // (envoyé vers dto : ce que l'API reçoit et envoie)
@NoArgsConstructor   // indispensable pour Hibernate/Génère un constructor sans paramètre
@AllArgsConstructor // Génére un constructor avec paramètre
@Builder // (pour la construction de la base de donnée)
public class User {
    @Id // Identifiant (quand un utilisateur se connecte ou s'inscrit, il récupère son identifiant unique et non similaire à un autre)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Faire en sorte que l'email soit unique (à travailler plus tard via JWT)
    private String email;

    @Column(name = "password", nullable = false) // Stocke le mot de passe HACHÉ (BCrypt), jamais en clair
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Type d'utilisateur : CLIENT, LAWYER ou ADMIN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
