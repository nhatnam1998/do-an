/**
 * author Nambui
 */
package epu.aeshop.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;

import epu.aeshop.entity.Advert;
import epu.aeshop.entity.Category;
import epu.aeshop.entity.Product;
import epu.aeshop.service.AdvertService;
import epu.aeshop.service.CategoryService;
import epu.aeshop.service.ProductService;
import epu.aeshop.vo.ProductVO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class SearchElasticController {

    @Value("${elasticsearch.url}")
    private String elasticsearchUrl;

	@Autowired
	private ProductService productService;

	@Autowired
	private AdvertService advertService;

	@Autowired
	private CategoryService categoryService;

	//search by elastic search
	@GetMapping("/search")
	public String indexSearch(@RequestParam("searchWord") String searchWord ,Model model) throws Exception {
		List<Product> products = new ArrayList<Product>();
			if(searchWord == null) {
				products = productService.getAll();
			}else {
				products = productService.searchByES(searchWord);
			}
			 Collections.shuffle(products, new Random());
			 model.addAttribute("products", products);
			 List<Advert> adverts = advertService.getAdverts();
				model.addAttribute("adverts", adverts);
				List<Category> categories = categoryService.getCategories();
				model.addAttribute("categories", categories);
		 return "index";
	}

	@GetMapping("/run-index")
	@ResponseBody
	public String indexES() {
//		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		 if(authentication != null) {
//			 authentication.getName().equals(anObject)
//		 }
		try {
			this.runIndexES();
			return "success";
		} catch (Exception e) {
			log.error(e);
			return "403";
		}
	}

	private void runIndexES() throws Exception {
		String indexlink = this.elasticsearchUrl + "product/";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		//delete Index
		try {
			restTemplate.delete(indexlink);
			log.info("=== DELETE SUCCESS ===");
		}catch (HttpClientErrorException e) {
			log.error(e);
		}

		//create index
		String requestParam ="{\n"
				+ "  \"mappings\": {\n"
				+ "    \"properties\": {\n"
				+ "      \"id\": { \"type\": \"long\" },\n"
				+ "      \"name\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"description\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"origin\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"brand\": {\n"
				+ "        \"type\": \"text\",\n"
				+ "        \"fields\": {\n"
				+ "          \"keyword\": { \"type\": \"keyword\" }\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  },\n"
				+ "  \"settings\": { \"number_of_shards\": 3 }\n"
				+ "}";
		HttpEntity<String> request = new HttpEntity<String>(requestParam,headers);
		try {
			restTemplate.exchange(indexlink
//					+ "mapping/product"
					, HttpMethod.PUT, request,Object.class);
			log.info("=== CREATE INDEX SUCCESS ===");
		}catch (HttpClientErrorException e) {
			log.error(e);
		}

		//get Data to push Index
		List<ProductVO> lstProductVO = productService.getSearch();
		log.info("=== START INDEX ===");
		for(ProductVO productVO : lstProductVO) {
			log.info(productVO);
			JsonObject json = new JsonObject();
			json.addProperty("id", productVO.getId());
			json.addProperty("name", productVO.getName().toLowerCase());
			json.addProperty("description", productVO.getDescription() == null? null: productVO.getDescription().toLowerCase());
			json.addProperty("origin", productVO.getOrigin() == null? null: productVO.getOrigin().toLowerCase());
			json.addProperty("brand", productVO.getBrand() == null? null: productVO.getBrand().toLowerCase());

			HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);
			restTemplate.exchange(indexlink + "_doc"
					, HttpMethod.POST, entity,Object.class);
			log.info("============= INPUT DATA TO ES SUCESS ===============");
		}
		log.info("===INDEX END===");
	}
}
