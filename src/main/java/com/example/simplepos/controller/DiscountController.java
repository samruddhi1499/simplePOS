package com.example.simplepos.controller;


import com.example.simplepos.dto.DiscountDTO;
import com.example.simplepos.service.DiscountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api-discount")
public class DiscountController {

    private final DiscountService discountService;


    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<List<DiscountDTO>> getAllCategories(){

        List<DiscountDTO> discountDTOS = discountService.getAllDiscount();

        return new ResponseEntity<>(discountDTOS, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> addDiscount(@RequestBody DiscountDTO discountDTO){
        if(discountService.addDiscount(discountDTO))
            return new ResponseEntity<>(HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @PutMapping
    public ResponseEntity<HttpStatus> updateDiscount(@RequestBody DiscountDTO discountDTO){
        if(discountService.updateDiscount(discountDTO))
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCategory(@PathVariable Integer id){

        if( discountService.deleteDiscount(id) )
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);


    }



}
