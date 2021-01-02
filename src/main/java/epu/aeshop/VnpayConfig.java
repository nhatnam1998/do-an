package epu.aeshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import epu.aeshop.vnPay.service.VnpayService;

@Configuration
@PropertySource("classpath:payment.properties")
public class VnpayConfig {

    @Autowired
    Environment env;

    @Bean
    @Autowired
    public VnpayService getVnpayService() {
        return new VnpayService(
            env.getProperty("application.base_url"),
            env.getProperty("vnpay.pay_url"),
            env.getProperty("vnpay.website_id"),
            env.getProperty("vnpay.hash_secret")
        );
    }
}