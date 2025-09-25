package com.example.campussystem.service;

import com.example.campussystem.dto.ProductRequest;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.entity.Category;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.CategoryRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品服务类
 */
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheService cacheService;

    // 缓存键常量
    private static final String CACHE_KEY_POPULAR_PRODUCTS = "popular_products:";
    private static final String CACHE_KEY_LATEST_PRODUCTS = "latest_products:";
    private static final String CACHE_KEY_PRODUCT_DETAIL = "product_detail:";
    private static final String CACHE_KEY_CATEGORY_PRODUCTS = "category_products:";
    private static final String CACHE_KEY_USER_PRODUCTS = "user_products:";

    /**
     * 发布商品
     */
    public ProductResponse publishProduct(ProductRequest request, Long userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证分类是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("商品分类不存在"));

        // 创建商品实体
        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStatus(1); // 默认上架状态

        // 处理图片数据 - 直接存储为JSON数组格式
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                // 直接将List转换为JSON字符串，不会产生双重引号
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            product.setImages("[]"); // 空数组
        }

        // 设置商品详细信息
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags());

        // 保存商品
        Product savedProduct = productRepository.save(product);

        // 清除相关缓存
        clearProductCaches(savedProduct.getCategoryId(), userId);

        // 设置关联信息并返回
        savedProduct.setUser(user);
        savedProduct.setCategory(category);
        return new ProductResponse(savedProduct);
    }

    /**
     * 保存商品草稿
     */
    public ProductResponse saveDraft(ProductRequest request, Long userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证分类是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("商品分类不存在"));

        // 创建商品实体
        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStatus(2); // 草稿状态

        // 处理图片数据 - 直接存储为JSON数组格式
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                // 直接将List转换为JSON字符串，不会产生双重引号
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            product.setImages("[]"); // 空数组
        }

        // 设置商品详细信息
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags());

        // 保存商品
        Product savedProduct = productRepository.save(product);

        // 清除相关缓存
        clearProductCaches(savedProduct.getCategoryId(), userId);

        // 设置关联信息并返回
        savedProduct.setUser(user);
        savedProduct.setCategory(category);
        return new ProductResponse(savedProduct);
    }

    /**
     * 更新商品信息
     */
    public ProductResponse updateProduct(Long productId, ProductRequest request, Long userId) {
        // 查找商品并验证权限
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new BusinessException("商品不存在或无权限修改"));

        // 验证分类是否存在
        if (!request.getCategoryId().equals(product.getCategoryId())) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("商品分类不存在"));
        }

        Long oldCategoryId = product.getCategoryId();

        // 更新商品信息
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());

        // 处理图片数据 - 直接存储为JSON数组格式
        if (request.getImages() != null) {
            try {
                // 直接将List转换为JSON字符串，不会产生双重引号
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片数据格式错误");
            }
        }

        // 更新商品详细信息
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setColor(request.getColor());
        product.setSize(request.getSize());
        product.setPurchaseTime(request.getPurchaseTime());
        product.setCondition(request.getCondition());
        product.setPurchaseLocation(request.getPurchaseLocation());
        product.setTradeNotes(request.getTradeNotes());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setIncludeInvoice(request.getIncludeInvoice());
        product.setIncludeWarranty(request.getIncludeWarranty());
        product.setTags(request.getTags());

        // 保存更新
        Product updatedProduct = productRepository.save(product);
        
        // 清除相关缓存
        clearProductCaches(oldCategoryId, userId);
        if (!oldCategoryId.equals(request.getCategoryId())) {
            clearProductCaches(request.getCategoryId(), userId);
        }
        // 清除商品详情缓存
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
        
        return new ProductResponse(updatedProduct);
    }

    /**
     * 获取商品详情
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {
        // 先从缓存获取
        String cacheKey = CACHE_KEY_PRODUCT_DETAIL + productId;
        ProductResponse cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if (cachedProduct != null) {
            // 异步增加浏览次数
            incrementViewCountAsync(productId);
            return cachedProduct;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 加载关联的用户信息
        User user = userRepository.findById(product.getUserId())
                .orElse(null);
        if (user != null) {
            product.setUser(user);
        }

        // 加载关联的分类信息
        Category category = categoryRepository.findById(product.getCategoryId())
                .orElse(null);
        if (category != null) {
            product.setCategory(category);
        }

        // 增加浏览次数
        product.incrementViewCount();
        productRepository.save(product);

        ProductResponse response = new ProductResponse(product);
        
        // 缓存商品详情，缓存30分钟
        cacheService.set(cacheKey, response, 30, java.util.concurrent.TimeUnit.MINUTES);
        
        return response;
    }

    /**
     * 清除商品相关缓存
     */
    private void clearProductCaches(Long categoryId, Long userId) {
        // 清除分类商品缓存
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_PRODUCTS + categoryId + ":*");
        // 清除用户商品缓存
        cacheService.delete(CACHE_KEY_USER_PRODUCTS + userId);
        // 清除热门商品缓存
        cacheService.deleteByPattern(CACHE_KEY_POPULAR_PRODUCTS + "*");
        // 清除最新商品缓存
        cacheService.deleteByPattern(CACHE_KEY_LATEST_PRODUCTS + "*");
    }

    /**
     * 增加商品浏览次数
     */
    public void incrementViewCount(Long productId) {
        productRepository.findById(productId).ifPresent(product -> {
            product.incrementViewCount();
            productRepository.save(product);
            // 清除缓存，下次查询时重新缓存
            cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
        });
    }

    /**
     * 异步增加浏览次数
     */
    private void incrementViewCountAsync(Long productId) {
        // 这里可以使用异步方式更新浏览次数，避免影响查询性能
        // 简单实现：直接更新数据库
        productRepository.findById(productId).ifPresent(product -> {
            product.incrementViewCount();
            productRepository.save(product);
            // 清除缓存，下次查询时重新缓存
            cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
        });
    }

    /**
     * 删除商品（软删除）
     */
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new BusinessException("商品不存在或无权限删除"));

        product.setStatus(0); // 设置为已删除状态
        productRepository.save(product);
        
        // 清除相关缓存
        clearProductCaches(product.getCategoryId(), userId);
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
    }

    /**
     * 更新商品状态
     */
    public void updateProductStatus(Long productId, Integer status, Long userId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new BusinessException("商品不存在或无权限修改"));

        if (status < 0 || status > 3) {
            throw new BusinessException("商品状态值无效");
        }

        product.setStatus(status);
        productRepository.save(product);
        
        // 清除相关缓存
        clearProductCaches(product.getCategoryId(), userId);
        cacheService.delete(CACHE_KEY_PRODUCT_DETAIL + productId);
    }

    /**
     * 获取商品列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductList(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productRepository.findByStatus(1, pageable); // 只查询上架商品
        return products.map(ProductResponse::new);
    }

    /**
     * 搜索商品
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Long categoryId, 
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, 1, pageable);
        return products.map(ProductResponse::new);
    }

    /**
     * 根据分类获取商品列表
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        String cacheKey = CACHE_KEY_CATEGORY_PRODUCTS + categoryId + ":" + page + ":" + size + ":" + sortBy + ":" + sortDir;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        Page<ProductResponse> cachedProducts = cacheService.get(cacheKey, Page.class);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
        
        // 为每个商品加载用户信息
        Page<ProductResponse> result = products.map(product -> {
            // 加载关联的用户信息
            User user = userRepository.findById(product.getUserId()).orElse(null);
            if (user != null) {
                product.setUser(user);
            }
            
            // 加载关联的分类信息
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                product.setCategory(category);
            }
            
            return new ProductResponse(product);
        });
        
        // 缓存分类商品，缓存20分钟
        cacheService.set(cacheKey, result, 20, java.util.concurrent.TimeUnit.MINUTES);
        
        return result;
    }

    /**
     * 获取用户发布的商品列表（支持分页和筛选）
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getUserProductsWithPagination(Long userId, int page, int size, Integer status, String title) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<Product> products;
        if (status != null && title != null && !title.trim().isEmpty()) {
            // 同时按状态和标题筛选
            products = productRepository.findByUserIdAndStatusAndTitleContainingIgnoreCase(userId, status, title.trim(), pageable);
        } else if (status != null) {
            // 只按状态筛选
            products = productRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (title != null && !title.trim().isEmpty()) {
            // 只按标题筛选
            products = productRepository.findByUserIdAndTitleContainingIgnoreCaseAndStatusNot(userId, title.trim(), 0, pageable);
        } else {
            // 不筛选，只排除已删除的商品
            products = productRepository.findByUserIdAndStatusNot(userId, 0, pageable);
        }
        
        // 转换为响应对象
        return products.map(product -> {
            // 加载关联的用户信息
            User user = userRepository.findById(product.getUserId()).orElse(null);
            if (user != null) {
                product.setUser(user);
            }
            
            // 加载关联的分类信息
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                product.setCategory(category);
            }
            
            return new ProductResponse(product);
        });
    }

    /**
     * 获取用户发布的商品列表
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getUserProducts(Long userId) {
        String cacheKey = CACHE_KEY_USER_PRODUCTS + userId;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<ProductResponse> cachedProducts = cacheService.get(cacheKey, List.class);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        List<Product> products = productRepository.findByUserIdAndStatusNot(userId, 0); // 排除已删除的商品
        
        // 为每个商品加载用户信息
        List<ProductResponse> result = products.stream()
                .map(product -> {
                    // 加载关联的用户信息
                    User user = userRepository.findById(product.getUserId()).orElse(null);
                    if (user != null) {
                        product.setUser(user);
                    }
                    
                    // 加载关联的分类信息
                    Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
                    if (category != null) {
                        product.setCategory(category);
                    }
                    
                    return new ProductResponse(product);
                })
                .collect(Collectors.toList());
        
        // 缓存用户商品，缓存10分钟
        cacheService.set(cacheKey, result, 10, java.util.concurrent.TimeUnit.MINUTES);
        
        return result;
    }

    /**
     * 获取热门商品
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getPopularProducts(int page, int size) {
        String cacheKey = CACHE_KEY_POPULAR_PRODUCTS + page + ":" + size;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        Page<ProductResponse> cachedProducts = cacheService.get(cacheKey, Page.class);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findPopularProducts(1, pageable);
        Page<ProductResponse> result = products.map(ProductResponse::new);
        
        // 缓存热门商品，缓存15分钟
        cacheService.set(cacheKey, result, 15, java.util.concurrent.TimeUnit.MINUTES);
        
        return result;
    }

    /**
     * 获取最新商品
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getLatestProducts(int page, int size) {
        String cacheKey = CACHE_KEY_LATEST_PRODUCTS + page + ":" + size;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        Page<ProductResponse> cachedProducts = cacheService.get(cacheKey, Page.class);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findLatestProducts(1, pageable);
        
        // 为每个商品加载用户信息
        Page<ProductResponse> result = products.map(product -> {
            // 加载关联的用户信息
            User user = userRepository.findById(product.getUserId()).orElse(null);
            if (user != null) {
                product.setUser(user);
            }
            
            // 加载关联的分类信息
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                product.setCategory(category);
            }
            
            return new ProductResponse(product);
        });
        
        // 缓存最新商品，缓存10分钟
        cacheService.set(cacheKey, result, 10, java.util.concurrent.TimeUnit.MINUTES);
        
        return result;
    }

    /**
     * 获取推荐商品
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findRecommendedProducts(
                product.getCategoryId(), productId, 1, pageable);
        
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 统计用户商品数量
     */
    @Transactional(readOnly = true)
    public long countUserProducts(Long userId) {
        return productRepository.countByUserIdAndStatusNot(userId, 0);
    }

    /**
     * 统计分类商品数量
     */
    @Transactional(readOnly = true)
    public long countCategoryProducts(Long categoryId) {
        return productRepository.countByCategoryIdAndStatus(categoryId, 1);
    }
}