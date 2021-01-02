package epu.aeshop.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import epu.aeshop.entity.Advert;
import epu.aeshop.entity.Category;
import epu.aeshop.entity.Product;
import epu.aeshop.service.AdvertService;
import epu.aeshop.service.BuyerService;
import epu.aeshop.service.CartService;
import epu.aeshop.service.CategoryService;
import epu.aeshop.service.ProductService;
import epu.aeshop.service.UserService;
import epu.aeshop.vo.ProductVO;
import lombok.extern.log4j.Log4j2;
import com.google.gson.JsonObject;

@Log4j2
@RestController
public class SearchElasticController {
	@Autowired
	ProductService productService;

	@Autowired
	private AdvertService advertService;

	@Autowired
	private UserService userService;

	@Autowired
	private BuyerService buyerService;

	@Autowired
	private CartService cartService;

	@Autowired
	private CategoryService categoryService;

//	@GetMapping("/elastic-search")
//    public String indexSearch(@RequestParam("searchWord") String searchWord ,Model model) {
//        List<ProductVO> products = new ArrayList<ProductVO>();
//		try {
//			products = productService.searchByES(searchWord);
//			 Collections.shuffle(products, new Random());
//		     model.addAttribute("products", products);
//		     List<Advert> adverts = advertService.getAdverts();
//		        model.addAttribute("adverts", adverts);
//		        List<Category> categories = categoryService.getCategories();
//		        model.addAttribute("categories", categories);
//		} catch (Exception e) {
//			return "403";
//		}
//
//		 return "index";
//       
//    }
//	
	@GetMapping("/run-index")
	 public String indexES() {
//		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		 if(authentication != null) {
//			 authentication.getName().equals(anObject)
//		 }
		 try {
			 this.runIndexES();
			 return "index";
		 }catch (Exception e) {
			 return "403";
		}
    }
	
	private void runIndexES() {
		String indexlink = "http://localhost:9200/";
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
		String requestParam = "{\"mappings\": { \"product\": {\"properties\": "
				+ "{\"id\": { \"type\": \"long\" },"
				+ "\"name\": {\"type\": \"text\", \"analyzer\":\"custom_lowercase_stemmed\",\"fields\": { \"keyword\": { \"type\":\"keyword\" }}},"
				+ "\"description\": {\"type\": \"text\", \"analyzer\":\"custom_lowercase_stemmed\",\"fields\": { \"keyword\": { \"type\":\"keyword\" }}},"
				+ "\"origin\": {\"type\": \"text\", \"analyzer\":\"custom_lowercase_stemmed\",\"fields\": { \"keyword\": { \"type\":\"keyword\" }}},"
				+ "\"brand\": {\"type\": \"text\", \"analyzer\":\"custom_lowercase_stemmed\",\"fields\": { \"keyword\": { \"type\":\"keyword\" }}}}}},"
				+ "\"settings\": { \"analysis\": {"
				+ " \"filter\":{"
				+ "\"custom_english_stemmer\": {\"type\": \"stemmer\",\"name\":\"english\"}},"
				+ "\"analyzer\": { "
				+ "\"custom_lowercase_stemmed\": { \"type\":\"custom\", \"tokenizer\": \"standard\", \"filter\": [ \"lowercase\",\"asciifolding\", \"custom_english_stemmer\"]},\"vi_analyzer\": { \"type\": \"custom\", \"tokenizer\":\"vi_tokenizer\", \"filter\": [ \"lowercase\", \"stop\"]}}}}}";
		HttpEntity<String> request = new HttpEntity<String>(requestParam,headers);
		try {
			restTemplate.exchange(indexlink, HttpMethod.PUT, request,Object.class);
			log.info("=== CREATE INDEX SUCCESS ===");
		}catch (HttpClientErrorException e) {
            log.error(e);
		}
		
		//get Data to push Index
		List<ProductVO> lstProductVO = productService.getSearch();
		log.info("=== START INDEX ===");
		for(ProductVO productVO : lstProductVO) {
			JsonObject json = new JsonObject();
			json.addProperty("id", productVO.getId());
			json.addProperty("name", productVO.getName());
			json.addProperty("description", productVO.getDescription());
			json.addProperty("origin", productVO.getOrigin());
			json.addProperty("brand", productVO.getBrand());
			try {
				HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);
			}catch(HttpClientErrorException e) {
				log.error(e);
			}
		}
		log.info("===INDEX END===");
		
	}
}
