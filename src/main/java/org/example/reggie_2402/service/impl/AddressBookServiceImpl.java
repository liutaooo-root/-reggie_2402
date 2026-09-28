package org.example.reggie_2402.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie_2402.entity.AddressBook;
import org.example.reggie_2402.mapper.AddressBookMapper;
import org.example.reggie_2402.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * 收货地址业务实现
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>
        implements AddressBookService {
}
