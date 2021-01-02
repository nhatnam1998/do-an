package epu.aeshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import epu.aeshop.domain.view.OrderInfo;
import epu.aeshop.entity.Buyer;
import epu.aeshop.entity.Order;
import epu.aeshop.entity.OrderItem;
import epu.aeshop.entity.OrderItemStatus;
import epu.aeshop.entity.OrderStatus;
import epu.aeshop.entity.Product;
import epu.aeshop.entity.User;
import epu.aeshop.service.BuyerService;
import epu.aeshop.service.OrderService;
import epu.aeshop.service.ProductService;
import epu.aeshop.service.UserService;
import epu.aeshop.util.Utils;
import epu.aeshop.vnPay.service.VnpayService;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private BuyerService buyerService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private VnpayService vnpayService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/buyer/orders/{orderId}")
    public String getOrder(@PathVariable("orderId") Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order.getStatus() == OrderStatus.PROCESSING) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getOrderStatus() == OrderItemStatus.ORDERED || item.getOrderStatus() == OrderItemStatus.SHIPPED) {
                    model.addAttribute("order", order);
                    Product product = productService.findById(item.getProduct().getId());
                    product.setAvailable(product.getAvailable() - new Double(item.getQuantity()));
                    return "/buyer/OrderDetail";
                }
            }
            order.setStatus(OrderStatus.COMPLETED);
            order.setEndDate(LocalDateTime.now());
            order.getBuyer().setPoints(order.getBuyer().getPoints() + order.getTotalAmount().divide(new BigDecimal(100)).intValue());
            orderService.updateOrder(order);
        }
        model.addAttribute("order", order);
        model.addAttribute("deliveredOrderItems", orderService.getDeliveredOrderItemsByOrder(orderId));
        return "/buyer/OrderDetail";
    }

    @PostMapping("/buyer/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        orderService.cancelOrder(order);
        return "redirect:/buyer/orders/" + orderId;
    }

    @PostMapping("/buyer/orders/{orderId}/download")
    public String downloadReceipt(@PathVariable("orderId") Long orderId, Model model, HttpServletResponse response) throws Exception {
        Order order = orderService.getOrderById(orderId);
        List<OrderItem> deliveredOrderItems = orderService.getDeliveredOrderItemsByOrder(orderId);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(order.getId());
        orderInfo.setBuyer(order.getBuyer());
        orderInfo.setRecipient(order.getRecipient());
        orderInfo.setPhoneNumber(order.getPhoneNumber());
        orderInfo.setPaymentInfo(order.getPaymentInfo());
        orderInfo.setTotalAmount(order.getTotalAmount());
        orderInfo.setOrderItems(deliveredOrderItems);
        File file = orderService.downloadReceipt(orderInfo);
        if (file.exists()) {
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=order.pdf");
            ServletOutputStream stream = null;
            BufferedInputStream buf = null;
            try {
                stream = response.getOutputStream();
                FileInputStream os = new FileInputStream(file);
                int readBytes = 0;
                byte [] buffer = new byte [4096];
                while ((readBytes = os.read (buffer,0,4096)) != -1) {
                    stream.write (buffer,0,readBytes);
                }
            } catch (IOException ioe) {
                throw new ServletException(ioe.getMessage());
            } finally {
                if(stream != null)
                    stream.close();
                if(buf != null)
                    buf.close();
            }
        }
        return "redirect:/buyer/orders/" + orderId;
    }

    @PostMapping("/buyer/orders/delete/{orderId}")
    public String deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return "redirect:/buyer/orders";
    }
    
    //use VNPay to pay this product
    
//    @PostMapping("/buyer/orders/{orderId}")
//	public String purchase(HttpServletRequest req, @PathVariable("orderId") Long orderId) throws IOException {
//    	Order order = orderService.getOrderById(orderId);	
//    	 order.setEndDate(LocalDateTime.now() );
//		String ip = Utils.getIpAddress(req);
////		order.setStatus(OrderStatus.PROCESSING);
//		return "redirect:" + vnpayService.getPaymentURL(order.getId().toString(), ip, order.getTotalAmount().intValue());
//	}
    
    @PostMapping("/buyer/order")
    public String placeOrder(HttpServletRequest req ,@Valid Order order) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        Buyer buyer = buyerService.getBuyerByUser(user);
        orderService.saveOrder(buyer, order);
        if(ObjectUtils.isEmpty(order.getTotalAmount())){
        	return "index";
        }
        order.setEndDate(LocalDateTime.now() );
		String ip = Utils.getIpAddress(req);
        return "redirect:" + vnpayService.getPaymentURL(order.getId().toString(), ip, order.getTotalAmount().intValue());
    }
    
    // source code support by VNpay 
    /** Vnpay IPN */
	@GetMapping("/vnpay/ipn")
	@ResponseBody
	public Map<String, String> vnpayIPN(HttpServletRequest request) throws IOException {
		Map<String, String> response = new HashMap<>();

		if (vnpayService.verifyRequest(request)) {

			Long id = Long.parseLong(request.getParameter("vnp_TxnRef"));
			Order order = orderService.getOrderById(id);
			//Kiem tra chu ky OK
			/* Kiem tra trang thai don hang trong DB: checkOrderStatus, 
			- Neu trang thai don hang OK, tien hanh cap nhat vao DB, tra lai cho VNPAY RspCode=00
			- Neu trang thai don hang (da cap nhat roi) => khong cap nhat vao DB, tra lai cho VNPAY RspCode=02
			*/
			boolean checkOrderStatus = order.getStatus() == null;

			if (checkOrderStatus) {
				if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
					//sucess
					order.setStatus(OrderStatus.CANCELED);
					order.setPaymentInfo(
						"VNPAY " +
						request.getParameter("vnp_CardType") +
						" " + 
						request.getParameter("vnp_TransactionNo") +
						" - " +
						request.getParameter("vnp_BankCode") +
						" " +
						request.getParameter("vnp_BankTranNo")
					);
				} else {
					// error pay
					order.setStatus(OrderStatus.COMPLETED);
				}
				
				orderService.updateOrder(order);
				response.put("RspCode", "00");
				response.put("Message", "Confirm Success");
			} else {
				//Don hang nay da duoc cap nhat roi, Merchant khong cap nhat nua (Duplicate callback)
				response.put("RspCode", "02");
				response.put("Message", "Order already confirmed");
			}
			
		} else {
			response.put("RspCode", "97");
			response.put("Message", "Invalid Checksum");
		}
		return response;
	}

}
