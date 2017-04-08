package com.mkyong.common.controller;

import com.mkyong.common.model.Car;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mkyong.common.model.Shop;

import java.util.List;

@Controller
@RequestMapping("/kfc/brands")
public class JSONController {

	@RequestMapping(value = "{name}", method = RequestMethod.GET)
	public @ResponseBody
	Shop getShopInJSON(@PathVariable String name) {

		System.out.println(name);

		Shop shop = new Shop();
		shop.setName(name);
		shop.setStaffName(new String[] { "mkyong1", "mkyong2" });

		return shop;

	}

	@RequestMapping(value = "/onecar", method = RequestMethod.POST)
	public ResponseEntity<Car> update(@RequestBody Car car) {

		if (car != null) {
			car.setMiles(car.getMiles() + 100);
		}

		return new ResponseEntity<Car>(car, HttpStatus.OK);
	}
}