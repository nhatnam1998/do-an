package epu.aeshop.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import epu.aeshop.entity.Category;
import epu.aeshop.entity.Product;
import epu.aeshop.entity.Seller;
import epu.aeshop.repository.ProductRepository;
import epu.aeshop.service.ProductService;
import epu.aeshop.vo.ProductVO;
import epu.aeshop.util.TextAnalyzer;

@Service
public class ProductServiceImpl implements ProductService {

    @Value("${elasticsearch.url}")
    private String elasticsearchUrl;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAll() {
        return (List<Product>) productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id).get();
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        productRepository.delete(product);
    }

    @Override
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findProductsByCategory(category);
    }

    @Override
    public List<Product> getProductsBySeller(Seller seller) {
        return productRepository.findProductsBySeller(seller);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findProductsByName(name);
    }

    @Override
    public List<ProductVO> getSearch() {
        List<ProductVO> lstResult = productRepository.getSearch();
        return lstResult;
    }

    @Override
    public List<Product> searchByES(String searchWord) throws Exception {
        return this.getDataFromElastic(searchWord);
    }

    private List<Product> getDataFromElastic(String searchText) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        RestTemplate rs = new RestTemplate();
        String text = TextAnalyzer.preprocess(searchText);
        headers.setContentType(MediaType.APPLICATION_JSON);
        rs.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String paramBody = "{\n"
                + "    \"query\": {\n"
                + "        \"query_string\" : {\n"
                + "            \"query\" : \"" + text + "\",\n"
                + "            \"fields\": [\"name\", \"description^3\"]\n"
                + "        }\n"
                + "    },\n"
                + "    \"_source\": [\"id\"]\n"
                + "}";

        HttpEntity<String> entity = new HttpEntity<String>(paramBody, headers);
        String indexlink = this.elasticsearchUrl + "product/";
        ResponseEntity<String> results = rs.exchange(indexlink + "/_search", HttpMethod.POST,
                entity, String.class);
        String str = results.getBody();
        JsonParser parserGSON = new JsonParser();
        JsonElement jsonTree = parserGSON.parse(str);
        JsonObject histParent = null;
        JsonArray histChild = null;
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            histParent = jsonObject.getAsJsonObject("hits");
            histChild = histParent.getAsJsonArray("hits");
        }

        List<Long> listIdProduct = new ArrayList<Long>();
        for (int i = 0; i < histChild.size(); i++) {
            JsonObject object = (JsonObject) histChild.get(i);
            JsonObject source = object.getAsJsonObject("_source");

            String id = (!source.get("id").toString().equals("-")) ? source.get("id").getAsString() : "0";
                    listIdProduct.add(Long.valueOf(id));
        }
        List<Product> lstResult = productRepository.getDataForElasticSearch(listIdProduct);

        return lstResult;
    }

    @Override
    public void reIndexIPContent(Product product) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        String indexLink = this.elasticsearchUrl + "product/";
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<String> resultSearch = null;

        resultSearch = restTemplate.exchange(indexLink + "/_search?q=id:" + product.getId(), HttpMethod.POST, null, String.class);
        if (resultSearch != null) {
            String indexID = "";
            String fIp = resultSearch.getBody();
            JsonParser parserGSON = new JsonParser();
            JsonElement jsonTree = parserGSON.parse(fIp);
            JsonObject histParent = null;
            JsonArray histChild = null;
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                histParent = jsonObject.getAsJsonObject("hits");
                histChild = histParent.getAsJsonArray("hits");
                if (histChild.size() > 0) {
                    JsonObject fo = (JsonObject) histChild.get(0);
                    indexID = (!fo.get("_id").toString().equals("null"))
                            ? fo.get("_id").getAsString() : "";
                }
            }
            if(!indexID.isEmpty()) {
                // delete index
                HttpEntity<String> bodyDelete = new HttpEntity<String>(headers);
                ResponseEntity<String> responseDel = restTemplate.exchange(indexLink + "_doc/" + indexID, HttpMethod.DELETE, bodyDelete, String.class);
            }

            JsonObject productNew = product.toValueObject().toElasticsearchDocument();

            HttpEntity<String> entity = new HttpEntity<String>(productNew.toString(), headers);
            restTemplate.exchange(indexLink + "_doc"
                    , HttpMethod.POST, entity,Object.class);
        }
    }

}
