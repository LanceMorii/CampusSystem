package com.example.campussystem.repository;

import com.example.campussystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据学号查找用户
     *
     * @param studentId 学号
     * @return 用户信息
     */
    Optional<User> findByStudentId(String studentId);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return 是否存在
     */
    boolean existsByStudentId(String studentId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据学号和状态查找用户
     *
     * @param studentId 学号
     * @param status 状态
     * @return 用户信息
     */
    Optional<User> findByStudentIdAndStatus(String studentId, Integer status);

    /**
     * 统计活跃用户数量
     *
     * @return 活跃用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActiveUsers();

    /**
     * 根据用户名模糊查询用户
     *
     * @param username 用户名
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username% AND u.status = 1")
    java.util.List<User> findByUsernameContaining(@Param("username") String username);

    /**
     * 根据状态统计用户数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定时间后创建的用户数量
     */
    long countByCreateTimeAfter(LocalDateTime createTime);

    /**
     * 统计指定时间后更新的用户数量
     */
    long countByUpdateTimeAfter(LocalDateTime updateTime);

    /**
     * 多条件搜索用户（用户名、真实姓名、学号）
     */
    Page<User> findByUsernameContainingIgnoreCaseOrRealNameContainingIgnoreCaseOrStudentIdContaining(
            String username, String realName, String studentId, Pageable pageable);
}