package juribook.lawyer_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString
public class Address {

    @Column(name = "address_street", length = 255)
    private String street;

    @Column(name = "address_postal_code", length = 10)
    private String postalCode;

    @Column(name = "address_city", nullable = false, length = 100)
    private String city;

    @Column(name = "address_country", nullable = false, length = 2)
    @Builder.Default
    private String country = "FR";
}