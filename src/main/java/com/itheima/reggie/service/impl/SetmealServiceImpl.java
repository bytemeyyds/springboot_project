package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要关联菜品
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //遍历每个setmealDishe，使得setmealDishe的setmealid与菜品表相等
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());


        //保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //判断套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0){
            //如果不能删除，就抛出一个业务异常
            throw new CustomException("套餐正在售卖，不能删除");
        }

        //如果可以删除就先删除套餐表中的数据_setmeal表
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据_setmeal_dish
        setmealDishService.remove(queryWrapper1);

    }

    /**
     * 修改套餐信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal中的基本信息表
        this.updateById(setmealDto);

        //清理当前的套餐对应的菜品信息数据setmeal_dish，delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的菜品数据，对setmeal_dish表进行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId((setmealDto.getId()));
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 根据id查询套餐信息和菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐基本信息，从setmeal中查询
        Setmeal setmeal = this.getById(id);

        //将信息copy到setmealDto中
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询套餐对应的菜品基本信息，从setmeal_dish表中查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(dishes);
        return setmealDto;
    }
}
