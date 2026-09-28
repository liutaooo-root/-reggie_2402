package org.example.reggie_2402.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.reggie_2402.common.BaseContext;
import org.example.reggie_2402.common.CustomException;
import org.example.reggie_2402.common.R;
import org.example.reggie_2402.entity.AddressBook;
import org.example.reggie_2402.service.AddressBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器：地址数据按用户隔离，全部通过 BaseContext 获取当前用户 id
 */
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    private static final Logger log = LoggerFactory.getLogger(AddressBookController.class);

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增收货地址：请求方式 POST，参数为 JSON
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        //userId 从 ThreadLocal 中取，保证地址归属当前登录用户
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("新增收货地址：{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址：请求方式 PUT，路径 /addressBook/default
     * 先把当前用户的所有地址置为非默认，再把目标地址置为默认，保证一个用户只有一个默认地址
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault, 0);
        //update address_book set is_default = 0 where user_id = ?
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1);
        //update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据 id 查询地址（编辑页回显）：请求方式 GET，路径 /addressBook/{id}
     */
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        }
        return R.error("没有找到该地址");
    }

    /**
     * 查询当前用户的默认地址：请求方式 GET，路径 /addressBook/default
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook != null) {
            return R.success(addressBook);
        }
        return R.error("没有找到默认地址");
    }

    /**
     * 查询当前用户的全部地址：请求方式 GET，路径 /addressBook/list
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("查询地址列表：{}", addressBook);
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 修改收货地址：请求方式 PUT，参数为 JSON（含 id）
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        log.info("修改收货地址：{}", addressBook);
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            //防止通过修改接口绕过"唯一默认地址"约束
            throw new CustomException("请通过设置默认地址接口修改默认地址");
        }
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }

    /**
     * 删除收货地址：请求方式 DELETE，参数 ids
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除收货地址：{}", ids);
        addressBookService.removeByIds(ids);
        return R.success("地址删除成功");
    }
}
