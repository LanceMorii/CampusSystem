package com.example.campussystem.service;

import com.example.campussystem.dto.UserLoginRequest;
import com.example.campussystem.dto.UserLoginResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.dto.UserRegisterRequest;
import com.example.campussystem.dto.UserRegisterResponse;
import com.example.campussystem.dto.UserUpdateRequest;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.UserRepository;
import com.example.campussystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheService cacheService;

    // 缓存键常量
    private static final String CACHE_KEY_USER_PROFILE = "user_profile:";
    private static final String CACHE_KEY_USER_BY_STUDENT_ID = "user_by_student_id:";

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    public UserLoginResponse login(UserLoginRequest request) {
        // 1. 验证学号格式
        if (!isValidStudentId(request.getStudentId())) {
            throw new BusinessException("学号格式不正确");
        }

        // 2. 查找用户
        Optional<User> userOptional = userRepository.findByStudentId(request.getStudentId());
        if (userOptional.isEmpty()) {
            throw new BusinessException("学号或密码错误");
        }

        User user = userOptional.get();

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("学号或密码错误");
        }

        // 5. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getStudentId(), user.getUsername());

        // 6. 构建登录响应
        return new UserLoginResponse(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                token,
                LocalDateTime.now()
        );
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应
     */
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        // 1. 验证学号是否已存在
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号已存在");
        }

        // 2. 验证邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 3. 验证手机号是否已存在（如果提供了手机号）
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已存在");
            }
        }

        // 4. 创建用户实体
        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码加密
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1); // 默认状态为正常

        // 6. 保存用户
        User savedUser = userRepository.save(user);

        // 清除相关缓存
        clearUserCaches(savedUser.getStudentId());

        // 7. 构建响应
        return new UserRegisterResponse(
                savedUser.getId(),
                savedUser.getStudentId(),
                savedUser.getUsername(),
                savedUser.getRealName(),
                savedUser.getPhone(),
                savedUser.getEmail(),
                savedUser.getCreateTime()
        );
    }

    /**
     * 根据学号查找用户
     *
     * @param studentId 学号
     * @return 用户信息
     */
    public Optional<User> findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 验证学号格式
     *
     * @param studentId 学号
     * @return 是否有效
     */
    public boolean isValidStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        // 学号应为8-12位数字
        return studentId.matches("^[0-9]{8,12}$");
    }

    /**
     * 检查学号是否已存在
     *
     * @param studentId 学号
     * @return 是否存在
     */
    public boolean isStudentIdExists(String studentId) {
        return userRepository.existsByStudentId(studentId);
    }

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByPhone(phone);
    }

    /**
     * 获取用户资料
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        String cacheKey = CACHE_KEY_USER_PROFILE + userId;
        
        // 先从缓存获取
        UserProfileResponse cachedProfile = cacheService.get(cacheKey, UserProfileResponse.class);
        if (cachedProfile != null) {
            return cachedProfile;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setStudentId(user.getStudentId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());

        // 缓存用户资料，缓存30分钟
        cacheService.set(cacheKey, response, 30, TimeUnit.MINUTES);

        return response;
    }

    /**
     * 更新用户资料
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 验证手机号是否已被其他用户使用
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }

        // 更新用户信息
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());

        User updatedUser = userRepository.save(user);

        // 清除用户缓存
        clearUserCaches(updatedUser.getStudentId());

        UserProfileResponse response = new UserProfileResponse();
        response.setId(updatedUser.getId());
        response.setStudentId(updatedUser.getStudentId());
        response.setUsername(updatedUser.getUsername());
        response.setRealName(updatedUser.getRealName());
        response.setPhone(updatedUser.getPhone());
        response.setEmail(updatedUser.getEmail());
        response.setAvatar(updatedUser.getAvatar());
        response.setRole(updatedUser.getRole());
        response.setStatus(updatedUser.getStatus());
        response.setCreateTime(updatedUser.getCreateTime());
        response.setUpdateTime(updatedUser.getUpdateTime());

        return response;
    }

    /**
     * 根据学号获取用户（带缓存）
     */
    @Transactional(readOnly = true)
    public User getUserByStudentId(String studentId) {
        String cacheKey = CACHE_KEY_USER_BY_STUDENT_ID + studentId;
        
        // 先从缓存获取
        User cachedUser = cacheService.get(cacheKey, User.class);
        if (cachedUser != null) {
            return cachedUser;
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 缓存用户信息，缓存60分钟
            cacheService.set(cacheKey, user, 60, TimeUnit.MINUTES);
            return user;
        }
        
        return null;
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserCaches(String studentId) {
        // 清除学号缓存
        cacheService.delete(CACHE_KEY_USER_BY_STUDENT_ID + studentId);
        // 清除用户资料缓存（需要根据用户ID，这里简化处理）
        cacheService.deleteByPattern(CACHE_KEY_USER_PROFILE + "*");
    }
}