package com.ngocnb.sharemoney.config;

import com.ngocnb.sharemoney.entity.Fruit;
import com.ngocnb.sharemoney.entity.GroupName;
import com.ngocnb.sharemoney.entity.User;
import com.ngocnb.sharemoney.repository.FruitRepository;
import com.ngocnb.sharemoney.repository.GroupNameRepository;
import com.ngocnb.sharemoney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializationConfig implements CommandLineRunner {

    private final FruitRepository fruitRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupNameRepository groupNameRepository;

    /**
     * Initialize fruit data on application startup
     */
    @Override
    public void run(String... args) throws Exception {
        // ACCOUNT
        log.info("========================================");
        log.info("  INITIALIZING ADMIN USER");
        log.info("========================================");

        // Kiểm tra xem admin đã tồn tại chưa (tránh tạo trùng)
        if (userRepository.findUserByUsername("admin").isPresent()) {
            log.info("✓ Admin user already exists. Skipping.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456")) // ⭐ mã hoá trước khi lưu
                .roles(List.of("ROLE_ADMIN"))
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        userRepository.save(admin);

        log.info("✓ Admin user created successfully (username: admin)");
        log.info("========================================");


        log.info("========================================");
        log.info("  INITIALIZING MEMBER USER");
        log.info("========================================");


        // Kiểm tra xem ngoc đã tồn tại chưa (tránh tạo trùng)
        if (userRepository.findUserByUsername("ngoc").isPresent()) {
            log.info("✓ Admin user already exists. Skipping.");
            return;
        }
        User ngoc = User.builder()
                .username("ngoc")
                .password(passwordEncoder.encode("123456")) // ⭐ mã hoá trước khi lưu
                .roles(List.of("ROLE_MEMBER"))
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        userRepository.save(ngoc);
        log.info("✓ Member user created successfully (username: ngoc)");
        log.info("========================================");


        // GROUP
        log.info("========================================");
        log.info("  INITIALIZING GROUPNAME   ");
        log.info("========================================");
        // Check if database already has Group
        long existingGroupCount = groupNameRepository.count();
        if (existingGroupCount > 0) {
            log.info("✓ Database already has {} groups. Skipping initialization.", existingGroupCount);
            return;
        }

        log.info("Database is empty. Adding 3 sample groups...");

        List<GroupName> groupNames = Arrays.asList(
                GroupName.builder()
                        .name("Ăn ốc")
                        .build(),

                GroupName.builder()
                        .name("Ăn chè")
                        .build(),

                GroupName.builder()
                        .name("Ăn nướng")
                        .build()
        );

        groupNameRepository.saveAll(groupNames);
        log.info("✓ Successfully added {} groupNames to database", groupNames.size());
        log.info("========================================");


        // FRUIT
        log.info("========================================");
        log.info("  INITIALIZING FRUIT DATABASE DATA");
        log.info("========================================");

        // Check if database already has data
        long existingCount = fruitRepository.count();
        if (existingCount > 0) {
            log.info("✓ Database already has {} fruits. Skipping initialization.", existingCount);
            return;
        }

        log.info("Database is empty. Adding 30 sample fruits...");

        // Create 30 sample fruits
        List<Fruit> fruits = Arrays.asList(
                Fruit.builder()
                        .name("Mãng Cầu")
                        .desc("Mãng cầu chín mềm, vị ngọt thơm, giàu vitamin C, tốt cho tiêu hóa")
                        .price(new BigDecimal("35000"))
                        .build(),

                Fruit.builder()
                        .name("Sung")
                        .desc("Sung chín đỏ, vị ngọt tự nhiên, chứa enzym tiêu hóa, bổ máu")
                        .price(new BigDecimal("28000"))
                        .build(),

                Fruit.builder()
                        .name("Dừa")
                        .desc("Dừa tươi, nước dừa lạnh giải khát, cung cấp khoáng chất thiết yếu")
                        .price(new BigDecimal("25000"))
                        .build(),

                Fruit.builder()
                        .name("Đu Đủ")
                        .desc("Đu đủ chín vàng, vitamin A cao, làm sáng da, hỗ trợ tiêu hóa")
                        .price(new BigDecimal("22000"))
                        .build(),

                Fruit.builder()
                        .name("Xoài")
                        .desc("Xoài chín vàng, vị ngọt mịn, trái cây vua, giàu vitamin A và C")
                        .price(new BigDecimal("32000"))
                        .build(),

                // ============ TRÁI CÂY KHÁC (25 trái cây) ============
                Fruit.builder()
                        .name("Táo")
                        .desc("Táo đỏ ngon, giàu vitamin C, tốt cho sức khỏe tim mạch")
                        .price(new BigDecimal("25000"))
                        .build(),

                Fruit.builder()
                        .name("Chuối")
                        .desc("Chuối vàng chín, giàu kali, giúp tăng năng lượng và sức bền")
                        .price(new BigDecimal("15000"))
                        .build(),

                Fruit.builder()
                        .name("Cam")
                        .desc("Cam ngọt, giàu vitamin C, tăng cường miễn dịch, phòng chống cúm")
                        .price(new BigDecimal("30000"))
                        .build(),

                Fruit.builder()
                        .name("Dâu Tây")
                        .desc("Dâu tây tươi, chứa chất chống oxy hóa mạnh, giảm viêm")
                        .price(new BigDecimal("45000"))
                        .build(),

                Fruit.builder()
                        .name("Dứa")
                        .desc("Dứa ngon vàng, chứa enzyme bromelain, hỗ trợ tiêu hóa hiệu quả")
                        .price(new BigDecimal("28000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Hấu")
                        .desc("Dưa hấu lạnh, 90% nước, giải khát vào hè, giàu lycopene")
                        .price(new BigDecimal("20000"))
                        .build(),

                Fruit.builder()
                        .name("Nho Tím")
                        .desc("Nho tím chín, chứa resveratrol tốt cho tim, chống oxy hóa")
                        .price(new BigDecimal("50000"))
                        .build(),

                Fruit.builder()
                        .name("Việt Quất")
                        .desc("Việt quất xanh, chứa anthocyanin, tăng cường trí não sắc bén")
                        .price(new BigDecimal("60000"))
                        .build(),

                Fruit.builder()
                        .name("Mâm Xôi Đỏ")
                        .desc("Mâm xôi đỏ chín, giàu chất xơ, tốt cho tiêu hóa và giảm cân")
                        .price(new BigDecimal("55000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Tây")
                        .desc("Chanh tây, chua sảng khoái, sử dụng được nhiều cách nấu ăn")
                        .price(new BigDecimal("18000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Xanh")
                        .desc("Chanh xanh tươi, acid citric cao, khử khuẩn, làm sạch")
                        .price(new BigDecimal("20000"))
                        .build(),

                Fruit.builder()
                        .name("Kiwi Xanh")
                        .desc("Quả kiwi xanh, vitamin E cao, giúp chống lão hóa da")
                        .price(new BigDecimal("40000"))
                        .build(),

                Fruit.builder()
                        .name("Ổi Trắng")
                        .desc("Ổi trắng, giàu vitamin C, hỗ trợ miễn dịch, thanh lọc")
                        .price(new BigDecimal("16000"))
                        .build(),

                Fruit.builder()
                        .name("Chanh Leo")
                        .desc("Chanh leo, vị chua ngọt độc đáo, giàu chất xơ, tốt sức khỏe")
                        .price(new BigDecimal("38000"))
                        .build(),

                Fruit.builder()
                        .name("Thanh Long Tím")
                        .desc("Thanh long tím, ít calo, giàu vitamin, đẹp da tự nhiên")
                        .price(new BigDecimal("42000"))
                        .build(),

                Fruit.builder()
                        .name("Lê Vàng")
                        .desc("Lê vàng ngon, giàu chất xơ, hỗ trợ giảm cân an toàn")
                        .price(new BigDecimal("29000"))
                        .build(),

                Fruit.builder()
                        .name("Đào Tươi")
                        .desc("Đào tươi, vị ngọt mềm, giàu vitamin A và C, nuôi dưỡng da")
                        .price(new BigDecimal("31000"))
                        .build(),

                Fruit.builder()
                        .name("Lựu Đỏ")
                        .desc("Lựu đỏ, giàu chất chống oxy hóa, tốt cho tim mạch")
                        .price(new BigDecimal("65000"))
                        .build(),

                Fruit.builder()
                        .name("Mâm Xôi Đen")
                        .desc("Mâm xôi đen, vitamin K cao, giúp xương khỏe mạnh")
                        .price(new BigDecimal("58000"))
                        .build(),

                Fruit.builder()
                        .name("Dâu Đen")
                        .desc("Dâu đen, giàu sắt, hỗ trợ tạo máu, tăng năng lượng")
                        .price(new BigDecimal("52000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Lưới Cam")
                        .desc("Dưa lưới cam, vitamin A dồi dào, cắt lát ngon ngọt")
                        .price(new BigDecimal("35000"))
                        .build(),

                Fruit.builder()
                        .name("Dưa Lưới Xanh")
                        .desc("Dưa lưới xanh, ngọt tự nhiên, dễ tiêu hóa, bổ dưỡng")
                        .price(new BigDecimal("33000"))
                        .build(),

                Fruit.builder()
                        .name("Quýt Cam")
                        .desc("Quýt cam, vitamin C nhiều, dễ bóc vỏ, tiện ăn")
                        .price(new BigDecimal("26000"))
                        .build(),

                Fruit.builder()
                        .name("Bưởi Hồng")
                        .desc("Bưởi hồng, chua nhẹ, giúp giảm cân, hạ cholesterol")
                        .price(new BigDecimal("32000"))
                        .build(),

                Fruit.builder()
                        .name("Bơ")
                        .desc("Bơ chín mềm, chứa chất béo lành mạnh, làm trắng da hiệu quả")
                        .price(new BigDecimal("48000"))
                        .build(),

                Fruit.builder()
                        .name("Anh Đào Đỏ")
                        .desc("Anh đào đỏ, sắc đẹp tự nhiên, giàu chất chống viêm")
                        .price(new BigDecimal("70000"))
                        .build(),

                Fruit.builder()
                        .name("Mận Tím")
                        .desc("Mận tím, giàu chất xơ, hỗ trợ tiêu hóa tốt, nhuận tràng")
                        .price(new BigDecimal("27000"))
                        .build()
        );

        // Save all fruits to database
        fruitRepository.saveAll(fruits);

        log.info("✓ Successfully added {} fruits to database", fruits.size());
        log.info("========================================");
        log.info("  DATABASE INITIALIZATION COMPLETE");
        log.info("========================================");
    }
}
