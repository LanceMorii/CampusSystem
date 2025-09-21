package com.example.campussystem.service;

import com.example.campussystem.entity.Category;
import com.example.campussystem.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品分类服务层
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheService cacheService;

    // 缓存键常量
    private static final String CACHE_KEY_ALL_CATEGORIES = "all_categories";
    private static final String CACHE_KEY_CATEGORY_TREE = "category_tree";
    private static final String CACHE_KEY_CATEGORY_BY_PARENT = "category_by_parent:";
    private static final String CACHE_KEY_CATEGORY_DETAIL = "category_detail:";

    /**
     * 创建分类
     */
    public Category createCategory(String name, Long parentId, Integer sortOrder) {
        // 检查同级分类中是否已存在相同名称
        Optional<Category> existingCategory = categoryRepository.findByNameAndParentId(name, parentId);
        if (existingCategory.isPresent()) {
            throw new RuntimeException("同级分类中已存在相同名称的分类");
        }

        // 如果没有指定排序值，自动设置为最大值+1
        if (sortOrder == null) {
            Integer maxSortOrder = categoryRepository.getMaxSortOrder(parentId);
            sortOrder = (maxSortOrder == null ? 0 : maxSortOrder) + 1;
        }

        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        category.setStatus(1);

        Category savedCategory = categoryRepository.save(category);
        
        // 清除相关缓存
        clearCategoryCaches();
        
        return savedCategory;
    }

    /**
     * 更新分类
     */
    public Category updateCategory(Long id, String name, Long parentId, Integer sortOrder, Integer status) {
        Category category = getCategoryById(id);

        // 检查是否修改了名称，如果修改了需要检查重名
        if (!category.getName().equals(name) || !category.getParentId().equals(parentId)) {
            Optional<Category> existingCategory = categoryRepository.findByNameAndParentId(name, parentId);
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                throw new RuntimeException("同级分类中已存在相同名称的分类");
            }
        }

        // 检查是否将分类移动到自己的子分类下（防止循环引用）
        if (parentId != null && !parentId.equals(0L) && isDescendant(parentId, id)) {
            throw new RuntimeException("不能将分类移动到自己的子分类下");
        }

        category.setName(name);
        if (parentId != null) {
            category.setParentId(parentId);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }
        if (status != null) {
            category.setStatus(status);
        }

        Category updatedCategory = categoryRepository.save(category);
        
        // 清除相关缓存
        clearCategoryCaches();
        
        return updatedCategory;
    }

    /**
     * 删除分类
     */
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // 检查是否有子分类
        if (categoryRepository.existsByParentId(id)) {
            throw new RuntimeException("该分类下还有子分类，无法删除");
        }

        // 检查是否有商品
        if (categoryRepository.hasProducts(id)) {
            throw new RuntimeException("该分类下还有商品，无法删除");
        }

        categoryRepository.delete(category);
        
        // 清除相关缓存
        clearCategoryCaches();
    }

    /**
     * 根据ID获取分类详情
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        String cacheKey = CACHE_KEY_CATEGORY_DETAIL + id;
        
        // 先从缓存获取
        Category cachedCategory = cacheService.get(cacheKey, Category.class);
        if (cachedCategory != null) {
            return cachedCategory;
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        // 缓存分类详情，缓存60分钟
        cacheService.set(cacheKey, category, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        return category;
    }

    /**
     * 获取所有分类列表
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<Category> cachedCategories = cacheService.get(CACHE_KEY_ALL_CATEGORIES, List.class);
        if (cachedCategories != null) {
            return cachedCategories;
        }

        List<Category> categories = categoryRepository.findByStatusOrderBySortOrderAsc(1);
        
        // 缓存所有分类，缓存60分钟
        cacheService.set(CACHE_KEY_ALL_CATEGORIES, categories, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        return categories;
    }

    /**
     * 获取所有启用的分类
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(1);
    }

    /**
     * 获取顶级分类列表
     */
    @Transactional(readOnly = true)
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findTopLevelCategories(1);
    }

    /**
     * 根据父分类ID获取子分类列表
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByParentId(Long parentId) {
        String cacheKey = CACHE_KEY_CATEGORY_BY_PARENT + parentId;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<Category> cachedCategories = cacheService.get(cacheKey, List.class);
        if (cachedCategories != null) {
            return cachedCategories;
        }

        List<Category> categories = categoryRepository.findByParentIdAndStatusOrderBySortOrderAsc(parentId, 1);
        
        // 缓存子分类，缓存30分钟
        cacheService.set(cacheKey, categories, 30, java.util.concurrent.TimeUnit.MINUTES);
        
        return categories;
    }

    /**
     * 获取分类树结构
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<Category> cachedTree = cacheService.get(CACHE_KEY_CATEGORY_TREE, List.class);
        if (cachedTree != null) {
            return cachedTree;
        }

        List<Category> topCategories = getTopLevelCategories();
        for (Category category : topCategories) {
            loadChildren(category);
        }
        
        // 缓存分类树，缓存60分钟
        cacheService.set(CACHE_KEY_CATEGORY_TREE, topCategories, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        return topCategories;
    }

    /**
     * 递归加载子分类
     */
    private void loadChildren(Category category) {
        List<Category> children = getCategoriesByParentId(category.getId());
        category.setChildren(children);
        for (Category child : children) {
            loadChildren(child);
        }
    }

    /**
     * 检查是否为后代分类（防止循环引用）
     */
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        if (ancestorId.equals(descendantId)) {
            return true;
        }
        
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(descendantId);
        for (Category child : children) {
            if (isDescendant(ancestorId, child.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启用/禁用分类
     */
    public Category toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        Category updatedCategory = categoryRepository.save(category);
        
        // 清除相关缓存
        clearCategoryCaches();
        
        return updatedCategory;
    }

    /**
     * 批量删除分类
     */
    public void deleteCategoriesBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteCategory(id);
        }
    }

    /**
     * 初始化基础分类数据
     */
    public void initializeBasicCategories() {
        // 检查是否已经有分类数据
        if (categoryRepository.count() > 0) {
            return;
        }

        // 创建基础分类
        Category electronics = createCategory("电子产品", 0L, 1);
        Category books = createCategory("图书教材", 0L, 2);
        Category clothing = createCategory("服装配饰", 0L, 3);
        Category sports = createCategory("运动户外", 0L, 4);
        Category daily = createCategory("生活用品", 0L, 5);
        Category other = createCategory("其他", 0L, 6);

        // 电子产品子分类
        createCategory("手机数码", electronics.getId(), 1);
        createCategory("电脑配件", electronics.getId(), 2);
        createCategory("耳机音响", electronics.getId(), 3);
        createCategory("智能设备", electronics.getId(), 4);

        // 图书教材子分类
        createCategory("专业教材", books.getId(), 1);
        createCategory("考试资料", books.getId(), 2);
        createCategory("小说文学", books.getId(), 3);
        createCategory("工具书", books.getId(), 4);

        // 服装配饰子分类
        createCategory("男装", clothing.getId(), 1);
        createCategory("女装", clothing.getId(), 2);
        createCategory("鞋靴", clothing.getId(), 3);
        createCategory("包包配饰", clothing.getId(), 4);

        // 运动户外子分类
        createCategory("运动鞋服", sports.getId(), 1);
        createCategory("健身器材", sports.getId(), 2);
        createCategory("户外用品", sports.getId(), 3);
        createCategory("球类运动", sports.getId(), 4);

        // 生活用品子分类
        createCategory("宿舍用品", daily.getId(), 1);
        createCategory("学习用品", daily.getId(), 2);
        createCategory("护肤美妆", daily.getId(), 3);
        createCategory("食品零食", daily.getId(), 4);
    }

    /**
     * 清除所有分类相关缓存
     */
    private void clearCategoryCaches() {
        cacheService.delete(CACHE_KEY_ALL_CATEGORIES);
        cacheService.delete(CACHE_KEY_CATEGORY_TREE);
        
        // 清除所有父分类缓存（这里简化处理，实际可以更精确地清除）
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_BY_PARENT + "*");
        cacheService.deleteByPattern(CACHE_KEY_CATEGORY_DETAIL + "*");
    }
}