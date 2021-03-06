/**
 * author Nambui
 */
package epu.aeshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;

import epu.aeshop.entity.Product;
import epu.aeshop.service.AdvertService;
import epu.aeshop.service.CategoryService;
import epu.aeshop.service.ProductService;
import epu.aeshop.vo.ProductVO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class SearchElasticController {

    @Value("${elasticsearch.url}")
    private String elasticsearchUrl;

	@Autowired
	private ProductService productService;

	@Autowired
	private AdvertService advertService;

	@Autowired
	private CategoryService categoryService;

	@GetMapping("/run-index")
	public String indexES() {
//		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		 if(authentication != null) {
//			 authentication.getName().equals(anObject)
//		 }
		try {
			this.runIndexES();
			return "success";
		}
		catch (Exception e) {
			log.error(e);
			return "failed";
		}
    }

	private void runIndexES() throws Exception {
		String indexlink = this.elasticsearchUrl + "product/";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		//delete Index
		restTemplate.delete(indexlink);
		log.info("=== DELETE SUCCESS ===");

		//create index
		String requestParam ="{\n"
				+ "  \"mappings\": {\n"
				+ "    \"properties\": {\n"
				+ "      \"id\": { \"type\": \"long\" },\n"
				+ "      \"name\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"analyzer\": \"vi_analyzer\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"description\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"analyzer\": \"vi_analyzer\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"origin\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"analyzer\": \"vi_analyzer\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"brand\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"analyzer\": \"vi_analyzer\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  },\n"
				+ "  \"settings\": { \"number_of_shards\": 3 }\n"
				+ "}";
		HttpEntity<String> request = new HttpEntity<String>(requestParam,headers);
		restTemplate.exchange(indexlink, HttpMethod.PUT, request,Object.class);
		log.info("=== CREATE INDEX SUCCESS ===");

		//get Data to push Index
		List<ProductVO> lstProductVO = productService.getSearch();
		log.info("=== START INDEX ===");
		for(ProductVO item : lstProductVO) {
			log.info(item);
			JsonObject json = item.toElasticsearchDocument();

			HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);
			restTemplate.exchange(indexlink + "_doc"
					, HttpMethod.POST, entity,Object.class);
			log.info("============= INPUT DATA TO ES SUCESS ===============");
		}
		log.info("===INDEX END===");
	}
}
