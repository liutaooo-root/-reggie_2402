package org.example.reggie_2402.dto;

import org.example.reggie_2402.entity.OrderDetail;
import org.example.reggie_2402.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;
    //订单详情集合  的size 就是共size件
    private List<OrderDetail> orderDetails;
	
}
