package epu.aeshop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class ProductVO {
	private Long id;
	private String name;
    private String description;
    private String origin;
    private String brand;

}
