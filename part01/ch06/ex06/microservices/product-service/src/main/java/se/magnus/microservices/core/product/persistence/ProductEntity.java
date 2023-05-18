package se.magnus.microservices.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products") // 몽고 DB 컬렉션과 매핑
public class ProductEntity {

    @Id
    private String id;

    /**
     * 버전은 새로 생성된 엔티티에서 0 부터 시작한다.
     */
    @Version
    private Integer version;

    /**
     * 비즈니스 키, productId 에 생성된 고유한 색인을 가져온다.
     */
    @Indexed(unique = true)
    private int productId;

    private String name;
    private int weight;

    // 기본 생성자
    public ProductEntity() {}

    public ProductEntity(int productId, String name, int weight) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
