package com.ngocnb.sharemoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Entry Point - Fruit Management System
 *
 * REST API để quản lý trái cây với các tính năng:
 * - JWT Authentication (đăng nhập, lấy token)
 * - CRUD operations cho Fruit
 * - Pagination, sorting, searching
 * - Swagger/OpenAPI documentation
 * - Comprehensive error handling
 * - 30 sample fruits pre-loaded
 *
 * Server: http://localhost:6969
 * Swagger: http://localhost:6969/swagger-ui.html
 *
 * Default credentials:
 * - Username: admin
 * - Password: 123456
 *
 * @author NgocNB
 * @version 2026.0217
 * @since 2026
 */
@SpringBootApplication
@EnableScheduling
public class ShareMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareMoneyApplication.class, args);

        // Print startup info
        printStartupInfo();
    }

    /**
     * Print startup information to console
     */
    private static void printStartupInfo() {
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════╗
                ║  📌 Server:    http://localhost:6969                      ║
                ║  📚 Swagger:   http://localhost:6969/swagger-ui.html      ║
                ║                                                           ║
                ║  🔑 Login Credentials:                                    ║
                ║     • Username: admin                                     ║
                ║     • Password: 123456                                    ║
                ╚═══════════════════════════════════════════════════════════╝
                
                """);
    }
}
