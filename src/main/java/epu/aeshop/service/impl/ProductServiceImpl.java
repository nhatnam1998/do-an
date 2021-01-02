package epu.aeshop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@Service
public class ProductServiceImpl implements ProductService {

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
		// TODO Auto-generated method stub
		return productRepository.getSearch();
	}
	
	@Override
	public List<ProductVO> searchByES(String searchWord) throws Exception{
		return this.getDataFromElastic(searchWord);
	}
	
	private List<ProductVO> getDataFromElastic(String searchText) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		RestTemplate rs = new RestTemplate();
		rs.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		if (searchText.contains("\"")) {
			searchText = searchText.replace("\"", "\\\"");
		}
		String paramBody = "{\"query\": { \"bool\": { \"must\": { \"multi_match\": { \"query\" : \"" + searchText
				+ "\", \"fields\": [\"contentSearch\", \"title^3\", \"summary\"]}}    ";

		paramBody = paramBody + "}},\"_source\": [\"id\", \"title\", \"summary\"]"
				+ ", \"highlight\": { \"pre_tags\": [\"<mark>\"], \"post_tags\": [\"</mark>\"], \"fields\" : {\"title\" : {},\"summary\" : {}}}}";

		HttpEntity<String> entity = new HttpEntity<String>(paramBody, headers);
		String indexLink = "http://localhost:9200/";
		ResponseEntity<String> results = rs.exchange(indexLink + "/_search?size=500", HttpMethod.POST, entity,
				String.class);
		String str = results.getBody();
		JsonParser parserGSON = new JsonParser();
		JsonElement jsonTree = parserGSON.parse(str);
		JsonObject histParent = null;
		JsonArray histChild = null;
//		if (jsonTree.isJsonObject()) {
//			JsonObject jsonObject = jsonTree.getAsJsonObject();
//			histParent = jsonObject.getAsJsonObject(Constants.FieldES.HITS);
//			histChild = histParent.getAsJsonArray(Constants.FieldES.HITS);
//		}
//
//		List<ProductVO> lstProduct = new ArrayList<ProductVO>();
//		List<String> listContentId = new ArrayList<String>();
//
//		for (int i = 0; i < histChild.size(); i++) {
//			JsonObject object = (JsonObject) histChild.get(i);
//			JsonObject source = object.getAsJsonObject(Constants.FieldES.SOURCE);
//			String contentId = (!source.get(Constants.FieldES.ID).toString().equals(Constants.FieldES.DATA_NULL))
//					? source.get(Constants.FieldES.ID).getAsString()
//					: "0";
//
//			ProductVO product = new ProductVO();
//
//			JsonObject highLightData = object.getAsJsonObject(Constants.FieldES.HIGHLIGHT);
//			if (highLightData != null) {
//				for (Entry<String, JsonElement> entry : highLightData.entrySet()) {
//					if (entry.getKey().contains("title")) {
//						product.setTitle(entry.getValue().getAsJsonArray().get(0).getAsString());
//					}
//					if (entry.getKey().contains("summary")) {
//						product.setSummary(entry.getValue().getAsJsonArray().get(0).getAsString());
//					}
//
//				}
//			}
//			ipContentVO.setContentId(Long.valueOf(contentId));
//			listContentId.add(contentId);
//			listIPPanelEls.add(ipContentVO);
//		}


		Map<List<String>, List<ProductVO>> result = new HashMap<List<String>, List<ProductVO>>();
		List<ProductVO> listResult = new ArrayList<ProductVO>();
//		result.put(listContentId, listIPPanelEls);
		return listResult;
	}
}
