package marcostar.project.store_project.entities;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import marcostar.project.store_project.entities.enums.TypePrivilege;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Privilege {
    @Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
    
    @Basic
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private TypePrivilege name;
    private String description;
    private String category;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
