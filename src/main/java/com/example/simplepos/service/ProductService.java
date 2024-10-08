package com.example.simplepos.service;

import com.example.simplepos.dto.InventoryDTO;
import com.example.simplepos.dto.ProductDTOGet;
import com.example.simplepos.dto.ProductDTOPost;
import com.example.simplepos.entity.*;
import com.example.simplepos.mapper.DTOMapper;
import com.example.simplepos.repository.InventoryRepository;
import com.example.simplepos.repository.ProductRepository;
import com.example.simplepos.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.ParseException;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductService {


    private final ProductRepository productRepository;


    private final ProductCategoryService productCategoryService;

    private final DiscountService discountService;
    private final WarehouseRepository warehouseRepository;

    private final InventoryService inventoryService;

    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryService productCategoryService, DiscountService discountService, WarehouseRepository warehouseRepository, InventoryService inventoryService, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryService = productCategoryService;
        this.discountService = discountService;
        this.warehouseRepository = warehouseRepository;
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
    }

    public Product getProductById(Long sku) {
        return productRepository.findById(sku).orElse(null);
    }

    public boolean addProduct(ProductDTOPost productDTOPost) throws ParseException {

        Product productCheck = productRepository.findById(productDTOPost.getProductSKU()).orElse(null);
        if(productCheck == null){

            Product product = new Product();
            Integer warehouseId = warehouseRepository.findByWarehouseName(productDTOPost.getWarehouseName());

            ProductCategory productCategory = productCategoryService.getProductCategoryByName(productDTOPost.getProductCategoryName());
            Discount discount = null;
            if(!productDTOPost.getDiscountName().isEmpty())
                discount = discountService.getDiscountByName(productDTOPost.getDiscountName());


            product.setSKU(productDTOPost.getProductSKU());
            product.setProductName(productDTOPost.getProductName());
            product.setProductDescription(productDTOPost.getProductDescription());
            product.setProductCategory(productCategory);
            product.setProductCostPrice(productDTOPost.getProductCostPrice());
            product.setProductCostPrice(productDTOPost.getProductCostPrice());
            product.setProductSellingPrice(productDTOPost.getProductSellingPrice());
            product.setIsExpirable(productDTOPost.getIsExpirable());
            product.setStorageType(productDTOPost.getStorageType());
            product.setDiscount(discount);
            if (productDTOPost.getProductImage().isEmpty()) {
                product.setProductImage(null);
            } else {
                product.setProductImage(productDTOPost.getProductImage());
            }
            productRepository.save(product);

            inventoryService.addToInventory(new InventoryDTO(productDTOPost.getProductSKU(),warehouseId, productDTOPost.getProductQuantity(), productDTOPost.getExpiryDate(),null,null,null));

            return true;
        }
        return false;
    }



    public List<ProductDTOGet> getAllProducts() {

        discountService.checkIfActive();
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .map(DTOMapper::toDTOGet)
                .collect(Collectors.toList());

    }

    public boolean updateProduct(ProductDTOPost productDTOPost) {

        Product product = productRepository.findById(productDTOPost.getProductSKU()).orElse(null);

        if(product != null){
            ProductCategory productCategory = productCategoryService.getProductCategoryByName(productDTOPost.getProductCategoryName());
            Discount discount = discountService.getDiscountByName(productDTOPost.getDiscountName());

            product.setProductName(productDTOPost.getProductName() != null && !productDTOPost.getProductName().isEmpty() ? productDTOPost.getProductName() : product.getProductName());
            product.setProductDescription(productDTOPost.getProductDescription() != null && !productDTOPost.getProductDescription().isEmpty() ? productDTOPost.getProductDescription() : product.getProductDescription());
            product.setProductCategory(productDTOPost.getProductCategoryName() != null && productDTOPost.getProductCategoryName().isEmpty() ? productCategory : product.getProductCategory());
            product.setProductCostPrice(productDTOPost.getProductCostPrice() != null && productDTOPost.getProductCostPrice() > 0 ? productDTOPost.getProductCostPrice() : product.getProductCostPrice());
            product.setProductSellingPrice(productDTOPost.getProductSellingPrice() != null && productDTOPost.getProductSellingPrice() > 0 ? productDTOPost.getProductSellingPrice() : product.getProductSellingPrice());
            product.setIsExpirable(productDTOPost.getIsExpirable() instanceof Boolean  ? productDTOPost.getIsExpirable() : product.getIsExpirable());
            product.setStorageType(productDTOPost.getStorageType() != null && !productDTOPost.getStorageType().isEmpty() ? productDTOPost.getStorageType() : product.getStorageType());
            product.setProductImage(productDTOPost.getProductImage() != null ? productDTOPost.getProductImage(): product.getProductImage());
            product.setDiscount(!productDTOPost.getDiscountName().isEmpty() && productDTOPost.getDiscountName() != null ? discount: product.getDiscount());
            productRepository.save(product);


            return true;
        }
        return false;

    }

    @Transactional
    public boolean deleteProduct(ProductDTOPost productDTOPost) {
        List<InventoryDTO> entryById = inventoryService.getEntryById(productDTOPost.getProductSKU());
        if( entryById.isEmpty() ){
            productRepository.deleteById(productDTOPost.getProductSKU());
            return true;
        }
        else{
            List<Inventory> bySKU = inventoryRepository.findBySKU(productDTOPost.getProductSKU());
            for(Inventory lineItem : bySKU){
                if(lineItem.getQuantity() == 0)
                    inventoryRepository.deleteById(new InventoryPKId(lineItem.getId().getProductSKU(), lineItem.getId().getWarehouseID(), lineItem.getId().getExpiryDate()));
            }
            List<InventoryDTO> entry = inventoryService.getEntryById(productDTOPost.getProductSKU());
            if(entry.isEmpty()){
                productRepository.deleteById(productDTOPost.getProductSKU());
                return true;
            }

            return false;
        }

    }

    public ProductDTOGet getEntryById(Long sku) {
        Product product = productRepository.findById(sku).orElse(null);
        return DTOMapper.toDTOGet(product);
    }
}
