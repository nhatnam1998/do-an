package epu.aeshop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.google.gson.JsonObject;
import epu.aeshop.util.TextAnalyzer;

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

    public JsonObject toElasticsearchDocument() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", TextAnalyzer.preprocess(name));
        json.addProperty("description", TextAnalyzer.preprocess(description));
        json.addProperty("origin", TextAnalyzer.preprocess(origin));
        json.addProperty("brand", TextAnalyzer.preprocess(brand));
        return json;
    }
}
