package edu.czjt.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.czjt.reggie.entity.Orders;


public interface OrderDetailService  {
 /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public void submitAgain(Orders orders);
}
