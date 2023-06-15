package edu.czjt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.czjt.reggie.common.R;
import edu.czjt.reggie.dto.DishDto;
import edu.czjt.reggie.entity.Category;
import edu.czjt.reggie.entity.Dish;
import edu.czjt.reggie.entity.DishFlavor;
import edu.czjt.reggie.service.CategoryService;
import edu.czjt.reggie.service.DishFlavorService;
import edu.czjt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaoshan, wangweiwei
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    /**
     * Created by wangxiaoshan
     */
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 根据分类查询菜品，并且为起售的菜品
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishService.list(queryWrapper);

        // 遍历dishs，创建List<DishDto>
        List<DishDto> dishDtos = dishes.stream().map((item) -> {
            // 创建DishDto
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 补充CategoryName
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            // 补充List<DishFlavor>
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(flavors);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.debug("通过ID：{} 获取菜品", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构建分页构造器对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(name != null, Dish::getName, name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        // 分页查询
        dishService.page(dishPage, dishLambdaQueryWrapper);

        // 将分页信息拷贝到dishDtoPage
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        // 将dishPage的record转为dishDtoPage的record
        List<Dish> records = dishPage.getRecords();

        List<DishDto> dishDtoList = records.stream().map((item) -> {
            return dish2dishDto(item);
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 将dish转化为dishDto
     *
     * @param dish
     * @return
     */
    private DishDto dish2dishDto(Dish dish) {
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish, dishDto);

        Category category = categoryService.getById(dish.getCategoryId());

        if (category != null) {
            dishDto.setCategoryName(category.getName());
        }

        List<DishFlavor> dishFlavors = dishFlavorService.getFlavorsByDishId(dish.getId());

        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }
