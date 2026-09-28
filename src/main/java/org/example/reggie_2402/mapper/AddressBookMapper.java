package org.example.reggie_2402.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie_2402.entity.AddressBook;

/**
 * 收货地址数据访问层
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
