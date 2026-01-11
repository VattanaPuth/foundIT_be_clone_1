package www.founded.com.model.seller;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.model.register.UserRegister;

@Data
@Entity
@Table(name = "seller_user")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_seller_id")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_user_register_id")
    private UserRegister user;  // The user who is the seller

    @Column(name = "username")
    private String name;  // Seller's name
    
    @Column(nullable = false)
    private boolean isPublic;  // Whether the seller’s profile is public
}

