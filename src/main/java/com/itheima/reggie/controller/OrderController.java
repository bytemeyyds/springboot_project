package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 展示历史订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){

        Page<Orders> pageInfo = new Page(page,pageSize);
        Page<OrderDto> orderDtoPage = new Page();
        Long currentId = BaseContext.getCurrentId();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,currentId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //构造条件构造器
        LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,orderDtoPage,"records");

        List<Orders> ordersList = pageInfo.getRecords();
        List<OrderDto> collect = ordersList.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item,orderDto);
            queryWrapper1.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);
            orderDto.setOrderDetails(list);
            return orderDto;
        }).collect(Collectors.toList());
        orderDtoPage.setRecords(collect);
        return R.success(orderDtoPage);
    }


}
